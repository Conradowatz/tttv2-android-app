package de.conradowatz.tttv2server;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaytimeFragment extends Fragment {

    private View thisView;
    private ListView listViewPlaytime;
    private ProgressBar progressBarPlaytime;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean firstView;
    private String prevPlaytime;
    private String[] playerID;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thisView = inflater.inflate(R.layout.fragment_playtime, container, false);

        listViewPlaytime = (ListView) thisView.findViewById(R.id.listView_playtime);
        progressBarPlaytime = (ProgressBar) thisView.findViewById(R.id.progressBar_playtime);

        mSwipeRefreshLayout = (SwipeRefreshLayout) thisView.findViewById(R.id.swipe_refresh_playtime);

        mSwipeRefreshLayout.setColorSchemeColors(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });

        firstView = true;
        listViewPlaytime.setVisibility(View.INVISIBLE);
        Refresh();
        return thisView;
    }

    private void Refresh() {


        new DownloadStringTask() {
            @Override
            protected void onPostExecute(String result) {
                RefreshList(result);
            }
        }.execute("http://tttv2api.conradowatz.de/tttv2playtime.php");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Get tracker.
        Tracker t = ((TTTv2Application) getActivity().getApplication()).getTracker(
                TTTv2Application.TrackerName.APP_TRACKER);

        // Set screen name.
        t.setScreenName("Spielzeit Rekorde");
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    public void RefreshList(String result) {

        if (firstView) {                                        //First time show spinner
            listViewPlaytime.setVisibility(View.VISIBLE);
            progressBarPlaytime.setVisibility(View.INVISIBLE);
        }
        firstView = false;

        mSwipeRefreshLayout.setRefreshing(false);

        if (!isAdded()) {   //Stops from crashing when being paused
            Refresh();
            return;
        }

        if (result != null) {

            if (result.equals(prevPlaytime)) {
                return;
            }
            prevPlaytime = result;

            String[] finishedArray = null;
            playerID = null;
            try {
                JSONArray completeArray = new JSONArray(result);
                int arrayLength = completeArray.length();
                String[] playerRank = new String[arrayLength];
                String[] playerName = new String[arrayLength];
                String[] playerTime = new String[arrayLength];
                playerID = new String[arrayLength];
                finishedArray = new String[arrayLength];
                for (int i=0; i<completeArray.length(); i++) {
                    JSONObject playerdata = completeArray.getJSONObject(i);
                    playerRank[i] = String.valueOf(playerdata.getInt("rank"));
                    playerName[i] = playerdata.getString("name");
                    playerTime[i] = playerdata.getString("time");
                    playerID[i] = playerdata.getString("uniqueid");

                    finishedArray[i] = playerRank[i] + ". " + playerName[i] + " (" + playerTime[i] + ")";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            ListAdapter myAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, finishedArray);
            listViewPlaytime.setAdapter(myAdapter);

            listViewPlaytime.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle("Profil aufrufen");
                    builder.setMessage("Bitte warten...");

                    // Set an EditText view to get user input
                    final ProgressBar waiting = new ProgressBar(getActivity());
                    builder.setView(waiting);

                    builder.setNeutralButton("Abbrechen", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    final AlertDialog alert = builder.create();
                    alert.show();

                    new DownloadStringTask() {
                        @Override
                        protected void onPostExecute(String result) {

                            if (result==null) {
                                alert.cancel();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                builder.setTitle("Profil aufrufen");
                                builder.setMessage(getString(R.string.no_connection_server));
                                builder.setNeutralButton("Ok", null);
                                builder.show();
                                return;
                            }

                            try {
                                JSONObject completeArray = new JSONObject(result);
                                String steamID = completeArray.getString("steamid");

                                Intent startProfile = new Intent(getActivity(), ProfileActivity.class);
                                startProfile.putExtra("SteamID", steamID);
                                alert.cancel();
                                startActivity(startProfile);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.execute("http://tttv2api.conradowatz.de/findsteamid.php?uniqueid=" + playerID[position]);



                }
            });

            listViewPlaytime.setVisibility(View.VISIBLE);
        } else {
            String[] Error = {getString(R.string.no_connection_server)};
            ListAdapter myAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, Error);
            listViewPlaytime.setAdapter(myAdapter);
            listViewPlaytime.setVisibility(View.VISIBLE);
            prevPlaytime = null; // make sure the ListView updates if connection is there again
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