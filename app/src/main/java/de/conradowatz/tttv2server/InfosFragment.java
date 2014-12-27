package de.conradowatz.tttv2server;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class InfosFragment extends Fragment {

    private View thisView;
    private TextView textView_map;
    private TextView textView_players;
    private ProgressBar progressBar_loading;
    private ListView listView_players;
    private TextView textView_mapchange;
    private TextView textView_loading_image;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView imageView_map;
    private TextView textViewNoConnection;

    private String imageFilePath;
    private boolean firstView;
    private String prevInfo;
    private String prevMap;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thisView = inflater.inflate(R.layout.fragment_infos, container, false);

        textView_map = (TextView) thisView.findViewById(R.id.textView_infos_map);
        textViewNoConnection = (TextView) thisView.findViewById(R.id.textView_info_noconnection);
        textView_players = (TextView) thisView.findViewById(R.id.textView_infos_players);
        progressBar_loading = (ProgressBar) thisView.findViewById(R.id.progressBar_infos);
        listView_players = (ListView) thisView.findViewById(R.id.listView_infos);
        textView_mapchange = (TextView) thisView.findViewById(R.id.textView_mapchange);
        textView_loading_image = (TextView) thisView.findViewById(R.id.textView_infos_image_loading);
        imageView_map = (ImageView) thisView.findViewById(R.id.imageView_infos_map);

        mSwipeRefreshLayout = (SwipeRefreshLayout) thisView.findViewById(R.id.swipe_refresh_infos);

        mSwipeRefreshLayout.setColorSchemeColors(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });

        firstView = true;
        imageView_map.setVisibility(View.INVISIBLE);
        textView_map.setVisibility(View.INVISIBLE);
        textView_players.setVisibility(View.INVISIBLE);
        listView_players.setVisibility(View.INVISIBLE);
        textView_mapchange.setVisibility(View.INVISIBLE);
        Refresh();
        return thisView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);

    }

    public void RefreshStrings(String result) {

        if (firstView) {
            progressBar_loading.setVisibility(View.INVISIBLE);      //first time show spinner
            textView_mapchange.setVisibility(View.INVISIBLE);
            textViewNoConnection.setVisibility(View.INVISIBLE);
            textView_map.setVisibility(View.VISIBLE);
            textView_players.setVisibility(View.VISIBLE);
            listView_players.setVisibility(View.VISIBLE);
        }
        firstView = false;

        mSwipeRefreshLayout.setRefreshing(false);               //Stop the Refresh Spinner
        progressBar_loading.setVisibility(View.INVISIBLE);
        textView_map.setVisibility(View.VISIBLE);

        if (!isAdded()) {   //Stops from crashing when being paused
            Refresh();
            return;
        }

        if (result == null) { //If no connection


            textViewNoConnection.setVisibility(View.VISIBLE);

            textView_loading_image.setVisibility(View.INVISIBLE);
            textView_map.setVisibility(View.INVISIBLE);
            textView_players.setVisibility(View.INVISIBLE);
            listView_players.setVisibility(View.INVISIBLE);
            firstView = true;
            return;
        }

        if (result.startsWith("no_connection")) { //If mapchange

            textView_mapchange.setText(R.string.text_mapchange);

            textView_mapchange.setVisibility(View.VISIBLE);
            textView_loading_image.setVisibility(View.INVISIBLE);
            textView_map.setVisibility(View.INVISIBLE);
            textView_players.setVisibility(View.INVISIBLE);
            listView_players.setVisibility(View.INVISIBLE);
            firstView = true;

            return;
        }

        //Information is legit
        //Set up the recieved information

        if (result.equals(prevInfo)) {  //do not refresh if it is the same
            return;
        }
        prevInfo = result;


        String[] playerArray = null;
        String serverMap = null;
        String playersNow = null;
        String playersMax = null;

        try {
            JSONObject completeArray = new JSONObject(result);
            JSONObject serverInfos = completeArray.getJSONObject("infos");
            JSONArray serverPlayers = completeArray.getJSONArray("players");

            serverMap = serverInfos.getString("map");
            playersNow = String.valueOf(serverInfos.getInt("playersnow"));
            playersMax = String.valueOf(serverInfos.getInt("maxplayers"));
            int playerArrayLength = serverPlayers.length();
            playerArray = new String[playerArrayLength];
            for (int i = 0; i < playerArrayLength; i++) {
                playerArray[i] = serverPlayers.getString(i);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        textView_map.setText(getString(R.string.text_infos_map) + " " + serverMap);
        textView_players.setText(getString(R.string.text_infos_player) + " " + playersNow + "/" + playersMax);

        ListAdapter myAdapter = null;
        if (playerArray.length != 0) {
            myAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, playerArray);
        } else {
            //if server is empty
            playerArray = new String[1];
            playerArray[0] = getString(R.string.server_empty);
            myAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, playerArray);
        }
        listView_players.setAdapter(myAdapter);
        setListViewHeightBasedOnChildren(listView_players);

        String imageFileName = serverMap + ".png";

        if (imageFileName.equals(prevMap)) { //do not refresh map if it is the same
            return;
        }
        prevMap = imageFileName;

        imageFilePath = thisView.getContext().getFilesDir() + "/" + imageFileName;

        File imageFile = new File(imageFilePath);
        if (imageFile.exists()) { //is map already saved?

            //Return map file

            FileInputStream inputStream;
            try {
                inputStream = thisView.getContext().openFileInput(imageFileName);
                Bitmap resultMapImage = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                RefreshImage(resultMapImage);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            //Download map file
            new DownloadImageTask() {
                @Override
                public void onPostExecute(Bitmap result) {
                    //Save map file

                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(imageFilePath);
                        result.compress(Bitmap.CompressFormat.PNG, 100, out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    RefreshImage(result);
                }
            }.execute("http://openload.conradowatz.de/maps/" + imageFileName);


        }


    }

    public void RefreshImage(Bitmap result) {

        if (result != null) {
            imageView_map.setImageBitmap(result);
            MainActivity.mMapImage.setImageBitmap(result);         //Change image in navigation drawer
        } else {
            imageView_map.setImageDrawable(getResources().getDrawable(R.drawable.unknown_map));
        }
        imageView_map.setVisibility(View.VISIBLE);
        textView_loading_image.setVisibility(View.INVISIBLE);


    }

    public void Refresh() {


        //Start downloading infos
        new DownloadStringTask() {
            @Override
            public void onPostExecute(String result) {
                RefreshStrings(result);
            }
        }.execute("http://tttv2api.conradowatz.de/tttv2infos.php");

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            Refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void setListViewHeightBasedOnChildren(ListView listView) {
        listView.setFocusable(false);
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup)
                listItem.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}