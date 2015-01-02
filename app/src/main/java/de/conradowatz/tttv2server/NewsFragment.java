package de.conradowatz.tttv2server;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class NewsFragment extends Fragment {
    private View thisView;
    private RecyclerView mRecylerView;
    private ProgressBar progressBarNews;
    private TextView textView_noconnection;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean firstView;
    private String prevNews;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thisView = inflater.inflate(R.layout.fragment_news, container, false);

        mRecylerView = (RecyclerView) thisView.findViewById(R.id.recyclerView_news);
        progressBarNews = (ProgressBar) thisView.findViewById(R.id.progressBar_news);
        textView_noconnection = (TextView) thisView.findViewById(R.id.textView_no_connection_news);

        mSwipeRefreshLayout = (SwipeRefreshLayout) thisView.findViewById(R.id.swipe_refresh_news);

        mSwipeRefreshLayout.setColorSchemeColors(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });

        firstView = true;
        mRecylerView.setVisibility(View.INVISIBLE);
        String[] test = {"test"};
        RecyclerNewsAdapter adapter = new RecyclerNewsAdapter(test, test, test);
        mRecylerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecylerView.setLayoutManager(llm);
        mRecylerView.setAdapter(adapter);
        Refresh();
        return thisView;
    }

    private void Refresh() {
        textView_noconnection.setVisibility(View.INVISIBLE);

        new DownloadStringTask() {
            @Override
            protected void onPostExecute(String result) {
                RefreshNews(result);
            }
        }.execute("http://conradowatz.de/category/tttv2-server?json=1");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Get tracker.
        Tracker t = ((TTTv2Application) getActivity().getApplication()).getTracker(
                TTTv2Application.TrackerName.APP_TRACKER);

        // Set screen name.
        t.setScreenName("News");
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    public void RefreshNews(String result) {

        mSwipeRefreshLayout.setRefreshing(false);

        if (firstView) {                                        //First time show spinner
            mRecylerView.setVisibility(View.VISIBLE);
            progressBarNews.setVisibility(View.INVISIBLE);
        }

        if (result == null) {
            mRecylerView.setVisibility(View.INVISIBLE);     //no connection
            textView_noconnection.setVisibility(View.VISIBLE);
            firstView = true;
            return;

        }

        firstView = false;

        if (!isAdded()) {   //Stops from crashing when being paused
            Refresh();
            return;
        }

        if (result.equals(prevNews)) {
            return;
        }
        prevNews = result;

        try {
            JSONObject everything = new JSONObject(result);
            JSONArray posts = everything.getJSONArray("posts");
            int postCount = posts.length();
            String[] titles = new String[postCount];
            String[] dates = new String[postCount];
            String[] contents = new String[postCount];

            for (int i = 0; i < postCount; i++) {
                JSONObject thisPost = posts.getJSONObject(i);
                titles[i] = thisPost.getString("title");
                dates[i] = thisPost.getString("date");
                contents[i] = thisPost.getString("content");
            }

            RecyclerNewsAdapter adapter = new RecyclerNewsAdapter(titles, dates, contents);

            mRecylerView.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mRecylerView.setLayoutManager(llm);
            mRecylerView.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
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