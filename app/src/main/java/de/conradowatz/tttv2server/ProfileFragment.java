package de.conradowatz.tttv2server;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    private View thisView;
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

    public static final String PREF_FILE_NAME="TTT_Profile";
    public static final String KEY_USER_LEARNED_DRAWER="MyProfile_SteamID";

    private boolean firstView;
    private String SteamID;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SteamID = readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "false");



        if (SteamID.equals("false")) {

            thisView = inflater.inflate(R.layout.fragment_new_profile, container, false);

            Button buttonNewProfile = (Button) thisView.findViewById(R.id.button_new_profile);


            buttonNewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditProfile();
                }
            });

            return thisView;
        }

        thisView = inflater.inflate(R.layout.fragment_profile, container, false);

        progressBar_loading = (ProgressBar) thisView.findViewById(R.id.progressBar_profile);
        mSwipeRefreshLayout = (SwipeRefreshLayout) thisView.findViewById(R.id.swipe_refresh_profile);
        completeLayout = (RelativeLayout) thisView.findViewById(R.id.relativeLayout_profile_main);

        imageViewAvatar = (ImageView) thisView.findViewById(R.id.imageView_profile_avatar);
        textViewName = (TextView) thisView.findViewById(R.id.textView_profile_name);
        textViewDmdeaths = (TextView) thisView.findViewById(R.id.textView_profile_dmdeaths);
        textViewDmkills = (TextView) thisView.findViewById(R.id.textView_profile_dmkills);
        textViewImageloading = (TextView) thisView.findViewById(R.id.textView_profile_image_loading);
        textViewDmkillrow = (TextView) thisView.findViewById(R.id.textView_profile_killrow);
        textViewPlaytime = (TextView) thisView.findViewById(R.id.textView_profile_playtime);
        textViewPspoints = (TextView) thisView.findViewById(R.id.textView_profile_pspoints);
        textViewDmtime = (TextView) thisView.findViewById(R.id.textView_profile_timedm);
        listViewWeapons = (ListView) thisView.findViewById(R.id.listView_profile_weapons);

        mSwipeRefreshLayout.setColorSchemeColors(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });
        completeLayout.setVisibility(View.INVISIBLE);

        firstView = true;
        Refresh();
        return thisView;
    }

    private void EditProfile() {
        AlertDialog.Builder alert = new AlertDialog.Builder(thisView.getContext());

        alert.setTitle("Profil anlegen");
        alert.setMessage("Bitte gib deinen Steam-Namen ein!");

        // Set an EditText view to get user input
        final EditText input = new EditText(thisView.getContext());
        alert.setView(input);

        alert.setPositiveButton("Suchen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                // Do something with value!
                StartNewSearch(value);

            }
        });

        alert.setNegativeButton("Abbrechen", null);

        alert.show();
    }

    private void StartNewSearch(String searched) {
        Intent startSearch = new Intent(thisView.getContext(), SearchableActivity.class);
        startSearch.setAction(Intent.ACTION_SEARCH);
        startSearch.putExtra(SearchManager.QUERY, searched);
        final int result = 1;
        startActivityForResult(startSearch, result);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data!=null) {
            String recievedSteamID = data.getStringExtra("SteamID");
            saveToPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, recievedSteamID);

            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.UpdateProfile();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Get tracker.
        Tracker t = ((TTTv2Application) getActivity().getApplication()).getTracker(
                TTTv2Application.TrackerName.APP_TRACKER);

        // Set screen name.
        t.setScreenName("Eigenes Profil");
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);

    }

    public void RefreshProfile(String result) {

        mSwipeRefreshLayout.setRefreshing(false);               //Stop the Refresh Spinner
        progressBar_loading.setVisibility(View.INVISIBLE);

        if (!isAdded()) {   //Stops from crashing when being paused
            Refresh();
            return;
        }

        if (result == null) { //If no connection
            firstView = true;
            TextView textView_noconnection = (TextView) thisView.findViewById(R.id.textView_profile_no_connection);
            completeLayout.setVisibility(View.INVISIBLE);
            textView_noconnection.setVisibility(View.VISIBLE);
            return;
        }

        TextView textView_noconnection = (TextView) thisView.findViewById(R.id.textView_profile_no_connection);
        completeLayout.setVisibility(View.VISIBLE);
        textView_noconnection.setVisibility(View.INVISIBLE);


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
        textViewPlaytime.setText("Spielzeit: " + playtime + " h");
        textViewPspoints.setText("PS Punkte: " + pspoints);
        textViewDmkills.setText("Kills: " + kills);
        textViewDmdeaths.setText("Tode: " + deaths);
        textViewDmkillrow.setText("Beste Kilstreak: " + kill_row);
        textViewDmtime.setText("Zeit im Deathmatch: " + time_dm);

        ListAdapter myAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, completeWeapons);
        listViewWeapons.setAdapter(myAdapter);
        setListViewHeightBasedOnChildren(listViewWeapons);

        FetchImage(SteamID);

    }

    private void FetchImage(String steamID) {

        new DownloadStringTask() {
            @Override
            protected void onPostExecute(String result) {
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

    public void Refresh() {


        //Start downloading profile
        new DownloadStringTask() {
            @Override
            public void onPostExecute(String result) {
                RefreshProfile(result);
            }
        }.execute("http://tttv2api.conradowatz.de/tttv2profile.php?steamid=" + SteamID);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_profile) {
            EditProfile();
            return true;
        } if (id == R.id.action_refresh) {
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

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }
}