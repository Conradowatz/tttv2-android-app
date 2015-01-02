package de.conradowatz.tttv2server;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class RulesFragment extends Fragment {
    private View thisView;
    private WebView mWebView;
    private ProgressBar progressBarRules;
    private TextView textView_noconnection;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thisView = inflater.inflate(R.layout.fragment_rules, container, false);

        mWebView = (WebView) thisView.findViewById(R.id.webView_rules);
        progressBarRules = (ProgressBar) thisView.findViewById(R.id.progressBar_rules);
        textView_noconnection = (TextView) thisView.findViewById(R.id.textView_no_connection_rules);

        Refresh();
        return thisView;
    }

    private void Refresh() {
        progressBarRules.setVisibility(View.VISIBLE);
        textView_noconnection.setVisibility(View.INVISIBLE);
        mWebView.setVisibility(View.INVISIBLE);

        new DownloadStringTask() {
            @Override
            protected void onPostExecute(String result) {
                RefreshRules(result);
            }
        }.execute("http://conradowatz.de/tttv2-server/server-regeln?json=1");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Get tracker.
        Tracker t = ((TTTv2Application) getActivity().getApplication()).getTracker(
                TTTv2Application.TrackerName.APP_TRACKER);

        // Set screen name.
        t.setScreenName("Regeln");
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    public void RefreshRules(String result) {

        progressBarRules.setVisibility(View.INVISIBLE);

        if (result == null) {
            textView_noconnection.setVisibility(View.VISIBLE);
            return;

        }

        mWebView.setVisibility(View.VISIBLE);

        if (!isAdded()) {   //Stops from crashing when being paused
            Refresh();
            return;
        }

        try {
            JSONObject everything = new JSONObject(result);
            JSONObject page = everything.getJSONObject("page");

            String content = page.getString("content");
            content = content.replaceAll("href=\"(.*?)\"", ""); //remove links

            mWebView.loadData(content, "text/html; charset=UTF-8", null);

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
            Refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}