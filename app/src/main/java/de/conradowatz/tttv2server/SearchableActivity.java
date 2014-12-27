package de.conradowatz.tttv2server;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SearchableActivity extends ActionBarActivity {

    private RecyclerView cardList;
    private String searchedName;
    private String[] nameArray;
    private String[] steamIDArray;
    private Bitmap[] imageArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_waiting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handleIntent(getIntent());

    }

    private void InitializeList(String result) {

        if (result==null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Profil aufrufen");
            builder.setMessage(getString(R.string.no_connection_server));
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
            setContentView(R.layout.fragment_error);

            Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            return;
        }
        if (result.startsWith("[]")) {
            NoResults();
            return;
        }

        try {
            JSONArray completeArray = new JSONArray(result);
            int arrayLength = completeArray.length();
            nameArray = new String[arrayLength];
            steamIDArray = new String[arrayLength];
            imageArray = new Bitmap[arrayLength];
            for (int i=0;i<arrayLength; i++) {
                JSONObject personArray = completeArray.getJSONObject(i);
                nameArray[i] = personArray.getString("name");
                steamIDArray[i] = personArray.getString("steamid");

                fetchImage(steamIDArray[i], i);

            }

            ShowList();


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void NoResults() {

        setContentView(R.layout.activity_profile_search_noresults);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textViewNoResults = (TextView) findViewById(R.id.textView_profile_search_noresults);
        String noResults = getString(R.string.text_profile_search_noresults).replace("%name%", searchedName);
        textViewNoResults.setText(noResults);

    }

    private void fetchImage(final String steamId, final int position) {

        new DownloadStringTask() {
            @Override
            protected void onPostExecute(String result) {

                if (result==null) {
                    return;
                }

                try {
                    JSONObject completeArray = new JSONObject(result);
                    String imageUrl = completeArray.getString("avatar");

                    getImage(imageUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void getImage(String imageUrl) {
                new DownloadImageTask() {
                    @Override
                    protected void onPostExecute(Bitmap result) {
                        if (result!=null){
                            imageArray[position] = result;
                            UpdateList();
                        } else {
                            fetchImage(steamId, position);
                        }

                    }
                }.execute(imageUrl);

            }
        }.execute("http://tttv2api.conradowatz.de/profilepic.php?steamid=" + steamId);

    }

    private void UpdateList() {

        RecyclerProfileAdapter cardListAdapter = new RecyclerProfileAdapter(nameArray, imageArray);

        cardList = (RecyclerView) findViewById(R.id.cardList_profile_search);
        cardList.setAdapter(cardListAdapter);

    }

    private void ShowList() {

        RecyclerProfileAdapter cardListAdapter = new RecyclerProfileAdapter(nameArray, imageArray);

        setContentView(R.layout.activity_profile_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cardList = (RecyclerView) findViewById(R.id.cardList_profile_search);
        cardList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        cardList.setLayoutManager(llm);
        cardList.setAdapter(cardListAdapter);

        TextView header = (TextView) findViewById(R.id.profile_search_header);
        header.setText("Suche nach '" + searchedName + "':");

        cardList.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                        Intent goingBack = new Intent();
                        goingBack.putExtra("SteamID", steamIDArray[position]);
                        setResult(RESULT_OK, goingBack);
                        finish();

                    }
                })
        );

    }

    @Override

    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);

    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchedName = intent.getStringExtra(SearchManager.QUERY);

            new DownloadStringTask() {
                @Override
                protected void onPostExecute(String result) {

                    InitializeList(result);

                }
            }.execute("http://tttv2api.conradowatz.de/tttv2profilesearch.php?name=" + searchedName);


        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.searchable, menu);
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
}
