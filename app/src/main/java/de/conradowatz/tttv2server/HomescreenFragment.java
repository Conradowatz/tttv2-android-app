package de.conradowatz.tttv2server;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class HomescreenFragment extends Fragment {

    private View thisView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thisView = inflater.inflate(R.layout.fragment_homescreen, container, false);

        return thisView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Get tracker.
        Tracker t = ((TTTv2Application) getActivity().getApplication()).getTracker(
                TTTv2Application.TrackerName.APP_TRACKER);

        // Set screen name.
        t.setScreenName("Homescreen");
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

        setHasOptionsMenu(true);

        new DownloadImageTask() {
            @Override
            protected void onPostExecute(Bitmap result) {
                RefreshImage(result);
            }
        }.execute("https://server.nitrado.net/deu/serverstatus/show/85.131.220.122/27015/1.png");

        super.onCreate(savedInstanceState);
    }

    public void RefreshImage(Bitmap onlineStateBitmap) {
        TextView textView_no_connection = (TextView) thisView.findViewById(R.id.textView_no_connection);
        ImageView imageViewonline = (ImageView) thisView.findViewById(R.id.imageViewonline);


        if (onlineStateBitmap!=null) {
            imageViewonline.setImageBitmap(onlineStateBitmap);
            imageViewonline.setVisibility(View.VISIBLE);
            textView_no_connection.setVisibility(View.INVISIBLE);
        } else {
            imageViewonline.setVisibility(View.INVISIBLE);
            textView_no_connection.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       // inflater.inflate(R.menu.homescreen, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        /*if (id == R.id.action_refresh_homescreen) {
            new DownloadImageTask(this)
                    .execute("https://server.nitrado.net/deu/serverstatus/show/85.131.220.122/27015/1.png");
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }
}