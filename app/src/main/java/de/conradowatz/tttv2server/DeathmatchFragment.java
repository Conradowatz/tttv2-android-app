package de.conradowatz.tttv2server;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeathmatchFragment extends Fragment {

    private View thisView;
    private ListView listViewDeathmatch;
    private ProgressBar progressBarDeathmatch;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean firstView;
    private String prevDM;
    private String[] playerSteamId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thisView = inflater.inflate(R.layout.fragment_deathmatch, container, false);

        listViewDeathmatch = (ListView) thisView.findViewById(R.id.listView_dm);
        progressBarDeathmatch = (ProgressBar) thisView.findViewById(R.id.progressBar_dm);

        mSwipeRefreshLayout = (SwipeRefreshLayout) thisView.findViewById(R.id.swipe_refresh_dm);

        mSwipeRefreshLayout.setColorSchemeColors(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });

        firstView = true;
        listViewDeathmatch.setVisibility(View.INVISIBLE);
        Refresh();
        return thisView;
    }

    private void Refresh() {


        new DownloadStringTask() {
            @Override
            protected void onPostExecute(String result) {
                RefreshList(result);
            }
        }.execute("http://tttv2api.conradowatz.de/tttv2dmkills.php");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    public void RefreshList(String result) {

        if (firstView) {                                        //First time show spinner
            listViewDeathmatch.setVisibility(View.VISIBLE);
            progressBarDeathmatch.setVisibility(View.INVISIBLE);
        }
        firstView = false;

        mSwipeRefreshLayout.setRefreshing(false);

        if (!isAdded()) {   //Stops from crashing when being paused
            Refresh();
            return;
        }

        if (result != null) {

            if (result.equals(prevDM)) {
                return;
            }
            prevDM = result;

            String[] finishedArray = null;
            playerSteamId = null;
            try {
                JSONArray completeArray = new JSONArray(result);
                int arrayLength = completeArray.length();
                String[] playerName = new String[arrayLength];
                String[] playerKills = new String[arrayLength];
                playerSteamId = new String[arrayLength];
                finishedArray = new String[arrayLength];
                for (int i=0; i<completeArray.length(); i++) {
                    JSONObject playerdata = completeArray.getJSONObject(i);
                    playerName[i] = playerdata.getString("name");
                    playerKills[i] = playerdata.getString("kills");
                    playerSteamId[i] = playerdata.getString("steamid");

                    finishedArray[i] = playerName[i] + " (" + playerKills[i] + ")";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            ListAdapter myAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, finishedArray);
            listViewDeathmatch.setAdapter(myAdapter);
            listViewDeathmatch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent startProfile = new Intent(getActivity(), ProfileActivity.class);
                    startProfile.putExtra("SteamID", playerSteamId[position]);
                    startActivity(startProfile);
                }
            });
        } else {
            String[] Error = {getString(R.string.no_connection_server)};
            ListAdapter myAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, Error);
            listViewDeathmatch.setAdapter(myAdapter);
            listViewDeathmatch.setVisibility(View.VISIBLE);
            prevDM = null; // make sure the ListView updates if connection is there again
        }


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
}