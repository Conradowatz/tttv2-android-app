package de.conradowatz.tttv2server;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class ProfileActivity extends ActionBarActivity {

    private ProgressBar progressBar_loading;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ImageView imageViewAvatar;
    private TextView textViewDmdeaths;
    private TextView textViewDmkills;
    private TextView textViewImageloading;
    private TextView textViewDmkillrow;
    private TextView textViewPlaytime;
    private TextView textViewPspoints;
    private TextView textViewDmtime;
    private TextView textViewName;
    private ListView listViewWeapons;
    private RelativeLayout completeLayout;

    private String SteamID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SteamID = getIntent().getStringExtra("SteamID");

        progressBar_loading = (ProgressBar) findViewById(R.id.progressBar_profile);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_profile);
        completeLayout = (RelativeLayout) findViewById(R.id.profile_layout);

        imageViewAvatar = (ImageView) findViewById(R.id.imageView_profile_avatar);
        textViewName = (TextView) findViewById(R.id.textView_profile_name);
        textViewDmdeaths = (TextView) findViewById(R.id.textView_profile_dmdeaths);
        textViewDmkills = (TextView) findViewById(R.id.textView_profile_dmkills);
        textViewImageloading = (TextView) findViewById(R.id.textView_profile_image_loading);
        textViewDmkillrow = (TextView) findViewById(R.id.textView_profile_killrow);
        textViewPlaytime = (TextView) findViewById(R.id.textView_profile_playtime);
        textViewPspoints = (TextView) findViewById(R.id.textView_profile_pspoints);
        textViewDmtime = (TextView) findViewById(R.id.textView_profile_timedm);
        listViewWeapons = (ListView) findViewById(R.id.listView_profile_weapons);

        completeLayout.setVisibility(View.INVISIBLE);

        mSwipeRefreshLayout.setColorSchemeColors(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });

        Refresh();
    }

    public void Refresh() {


        //Start downloading profile
        new DownloadStringTask() {
            @Override
            public void onPostExecute(String result) {
                RefreshProfile(result);
            }
        }.execute("http://tttv2api.conradowatz.de/tttv2profile.php?steamid=" + SteamID);

    }

    public void RefreshProfile(String result) {

        mSwipeRefreshLayout.setRefreshing(false);               //Stop the Refresh Spinner
        progressBar_loading.setVisibility(View.INVISIBLE);
        completeLayout.setVisibility(View.VISIBLE);

        if (result == null) { //If no connection

            setContentView(R.layout.fragment_error);

            Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            return;
        }


        String name = null;
        String playtime = null;
        String pspoints = null;
        String time_dm = null;
        String kills = null;
        String deaths = null;
        String kill_row = null;
        String weaponsstring = null;

        try {
            JSONObject completeArray = new JSONObject(result);
            name = completeArray.getString("name");

            if (name.startsWith("null")) {      //no profile on server
                setContentView(R.layout.fragment_error);
                TextView textViewError = (TextView) findViewById(R.id.textView_error);
                textViewError.setText(getString(R.string.error_no_profile_found));

                Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                return;
            }

            playtime = completeArray.getString("playtime");
            pspoints = completeArray.getString("pspoints");
            JSONObject datadm = completeArray.getJSONObject("datadm");
            time_dm = datadm.getString("time_dm");
            kills = datadm.getString("kills");
            deaths = datadm.getString("deaths");
            kill_row = datadm.getString("kill_row");
            weaponsstring = datadm.getString("weapons");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Integer[] weaponKills = null;
        String[] weaponNames = null;
        String[] completeWeapons = null;

        try {
            String[] weaponsArray = weaponsstring.split(",");
            int weaponCount = weaponsArray.length;
            weaponNames = new String[weaponCount];
            weaponKills = new Integer[weaponCount];
            completeWeapons = new String[weaponCount];
            for (int i = 0; i < weaponCount; i++) {
                weaponsArray[i] = weaponsArray[i].trim().replaceAll("(\\r|\\n)", "");
                String[] tmpSlpit = weaponsArray[i].split("=");
                weaponNames[i] = tmpSlpit[0];
                weaponKills[i] = Integer.valueOf(tmpSlpit[1]);
            }
            for (int n = 0; n < weaponKills.length; n++) {
                for (int m = 0; m < weaponKills.length-1 - n; m++) {
                    if ((weaponKills[m].compareTo(weaponKills[m + 1])) < 0) {
                        Integer swapInteger = weaponKills[m];
                        weaponKills[m] = weaponKills[m + 1];
                        weaponKills[m + 1] = swapInteger;
                        String swapStr = weaponNames[m];
                        weaponNames[m] = weaponNames[m + 1];
                        weaponNames[m + 1] = swapStr;
                    }
                }
            }
            for (int i = 0; i < weaponsArray.length; i++) {
                weaponNames[i] = weaponNames[i].replace("weapon_ghost_", "");
                weaponNames[i] = weaponNames[i].substring(0,1).toUpperCase() + weaponNames[i].substring(1);
                completeWeapons[i] = weaponNames[i] + ": " + String.valueOf(weaponKills[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            completeWeapons = new String[1];
            completeWeapons[0] = getString(R.string.dmstats_noweapons);
        }

        textViewName.setText(name);
        textViewPlaytime.setText(getString(R.string.playtime) + playtime + " h");
        textViewPspoints.setText(getString(R.string.ps_points) + pspoints);
        textViewDmkills.setText(getString(R.string.kills) + kills);
        textViewDmdeaths.setText(getString(R.string.deaths) + deaths);
        textViewDmkillrow.setText(getString(R.string.best_killstreak) + kill_row);
        textViewDmtime.setText(getString(R.string.time_dm) + time_dm);

        ListAdapter myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, completeWeapons);
        listViewWeapons.setAdapter(myAdapter);
        setListViewHeightBasedOnChildren(listViewWeapons);

        FetchImage(SteamID);

    }

    private void FetchImage(String steamID) {
        new DownloadStringTask() {
            @Override
            protected void onPostExecute(String result) {

                if (result==null) {
                    textViewImageloading.setText(R.string.no_connection_text);
                    return;
                }

                try {
                    JSONObject completeArray = new JSONObject(result);
                    String imgUrl = completeArray.getString("avatar");

                    new DownloadImageTask(){
                        @Override
                        protected void onPostExecute(Bitmap result) {
                            RefreshImage(result);
                        }
                    }.execute(imgUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute("http://tttv2api.conradowatz.de/profilepic.php?steamid=" + steamID);
    }

    public void RefreshImage(Bitmap result) {

        if (result != null) {
            imageViewAvatar.setImageBitmap(result);
        } else {
            FetchImage(SteamID);
        }
        textViewImageloading.setVisibility(View.INVISIBLE);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            Refresh();
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
                listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
