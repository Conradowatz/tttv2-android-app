package de.conradowatz.tttv2server;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class DownloadStringTask extends AsyncTask<String, Void, String> {

    private InputStream in;


    protected String doInBackground(String... urls) {
        String urldisplay = urls[0];
        String resultString = null;
        try {
            in = new java.net.URL(urldisplay).openStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");

            String line = "0";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            in.close();
            resultString = sb.toString();
        } catch (Exception e) {
            Log.d("STRING", "String download Error:");
            Log.e("STRING", e.getMessage());
        }

        return resultString;
    }

    protected void onPostExecute(String result) {

    }

}

