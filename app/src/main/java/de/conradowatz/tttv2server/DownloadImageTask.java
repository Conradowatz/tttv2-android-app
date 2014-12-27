package de.conradowatz.tttv2server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;


public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private InputStream in;


    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.d("IMAGE", "Image download Error:");
            Log.e("IMAGE", e.getMessage());
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {

    }


}
