package de.conradowatz.tttv2server;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private CharSequence mTitle;
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerFragment navigationDrawerFragment;
    private String[] nav_item_names;
    private int[] nav_item_iconids;
    public static ImageView mMapImage;
    private String updateChannel;
    private int versionNumber;

    private RecyclerView mRecyclerView;
    private RecyclerNavDrawAdapter mAdapter;

    private DownloadManager mgr = null;
    private long lastDownload = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapImage = (ImageView) findViewById(R.id.nav_draw_map);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        mTitle = getTitle();
        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigationDrawerFragment);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        setSupportActionBar(toolbar);
        navigationDrawerFragment.setUp(R.id.navigationDrawerFragment, mDrawerLayout, toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nav_item_names = getResources().getStringArray(R.array.nav_drawer_names); //ALLE
        nav_item_iconids = new int[]{R.drawable.ic_action_important,
                R.drawable.ic_action_view_as_list,
                R.drawable.ic_action_email,
                R.drawable.ic_action_error,
                0, R.drawable.ic_action_time,
                R.drawable.ic_action_mouse,
                0, R.drawable.ic_action_person,
                R.drawable.ic_action_search};

        mRecyclerView = (RecyclerView) mDrawerLayout.findViewById(R.id.nav_draw_recyclerview);
        mAdapter = new RecyclerNavDrawAdapter(nav_item_names, nav_item_iconids);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                DrawerItemClick(position);
            }
        }));
        DrawerItemClick(0);


        CheckForUpdate();


        mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    private void CheckForUpdate() {
        //Check for update

        try {
            final PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionNumber = pInfo.versionCode;

            updateChannel = "STABLE";
            if (pInfo.versionName.startsWith("BETA")) {
                updateChannel = "BETA";
                versionNumber = Integer.valueOf(pInfo.versionName.replace("BETA ", ""));
            }

            new DownloadStringTask() {
                @Override
                protected void onPostExecute(String result) {
                    if (result != null) {
                        try {
                            JSONObject completeArray = new JSONObject(result);
                            int latestVersion = completeArray.getInt("latest" + updateChannel.toLowerCase());
                            String downloadLink = completeArray.getString(updateChannel.toLowerCase() + "download");
                            if (latestVersion > versionNumber) {
                                NewVersionAvailable(downloadLink);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.execute("http://conradowatz.de/apps/tttv2/tttv2version.php");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void NewVersionAvailable(final String downloadLink) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.new_version_title));
        builder.setMessage(getString(R.string.new_version_text));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                startDownload(downloadLink);

            }
        });


        builder.setNegativeButton(getString(R.string.no_later), null);
        builder.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }

    public void UpdateProfile() {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_main, new ProfileFragment())
                .commit();
    }


    private void DrawerItemClick(int position) {

        FragmentManager fragmentManager = getFragmentManager();
        Fragment switchFragment = null;

        switch (position) {
            case 0:
                switchFragment = new HomescreenFragment();
                break;
            case 1:
                switchFragment = new InfosFragment();
                break;
            case 2:
                switchFragment = new NewsFragment();
                break;
            case 3:
                switchFragment = new RulesFragment();
                break;
            case 5:
                switchFragment = new PlaytimeFragment();
                break;
            case 6:
                switchFragment = new DeathmatchFragment();
                break;
            case 8:
                switchFragment = new ProfileFragment();
                break;
            case 9:
                ProfileSearch();
                break;
        }
        mTitle = nav_item_names[position];


        if (switchFragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_main, switchFragment)
                    .commit();
            getSupportActionBar().setTitle(mTitle);
            mAdapter.selectItem(position);
            mDrawerLayout.closeDrawer(findViewById(R.id.navigationDrawerFragment));
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null) {
            String SteamID = data.getStringExtra("SteamID");
            Intent startProfile = new Intent(this, ProfileActivity.class);
            startProfile.putExtra("SteamID", SteamID);
            startActivity(startProfile);
        }
    }

    private void ProfileSearch() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.search_profile_title));
        alert.setMessage(getString(R.string.search_profile_text));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.search), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                // Do something with value!
                Intent startSearch = new Intent(getApplicationContext(), SearchableActivity.class);
                startSearch.setAction(Intent.ACTION_SEARCH);
                startSearch.putExtra(SearchManager.QUERY, value);
                final int result = 1;
                startActivityForResult(startSearch, result);


            }
        });

        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(onComplete);
    }

    public void startDownload(String downloadLink) {
        Uri uri = Uri.parse(downloadLink);

        Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .mkdirs();

        File file = new File(Environment.getExternalStorageDirectory() + "/download/" + "tttv2update.apk");
        file.delete();

        if (file.exists()) {
            startDownload(downloadLink);
            return;
        }

        lastDownload =
                mgr.enqueue(new DownloadManager.Request(uri)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setTitle(getString(R.string.updater_title))
                        .setDescription(getString(R.string.updater_text))
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                "tttv2update.apk"));


    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            //open apk
            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            openIntent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "tttv2update.apk")), "application/vnd.android.package-archive");
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(openIntent);

        }
    };

}

