package nl.hva.wattbike.wattbike;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Sean Molenaar
 * @version 0.0.0.1
 * @since 1-5-15
 */

/**
 * Download files from the main thread with this.
 *
 * @see android.os.AsyncTask
 */
public class HeartBeatTask extends AsyncTask<String, Integer, String> {
    private TextView resultView;

    /**
     * Executed in background. Called by .execute(<String>)
     *
     * @param urls the urls you want to query
     * @return Output of the query
     */
    protected String doInBackground(String... urls) {
        String output;
        output = getOutputFromUrl(urls[0]);
        return output;
    }


    /**
     * Code to read the response of the server
     *
     * @param url the URL its gonna call
     * @return the response in string format
     */
    private String getOutputFromUrl(String url) {
        StringBuilder output = new StringBuilder("");
        try {
            InputStream stream = getHttpConnection(url);
            BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(stream));
            String s;
            while ((s = buffer.readLine()) != null)
                output.append(s);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return output.toString();
    }


    /**
     * Code to query the server, only called by getOutputFromUrl()
     *
     * @param urlString the URL to query
     * @return bytestream version of the response
     * @throws IOException
     */
    private InputStream getHttpConnection(String urlString)
            throws IOException {
        InputStream stream = null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return stream;
    }


    // This is called when doInBackground() is finished
    protected void onPostExecute(String result) {
        resultView.setText("...");
        resultView.setVisibility(View.VISIBLE);
        try {
            JSONObject object = new JSONObject(result);
            String status = object.getString("status");
            resultView.setText(status);
        } catch (JSONException e) {
            resultView.setText(result);
        }

    }

    public void setResultView(TextView t) {
        resultView = t;
    }
}
