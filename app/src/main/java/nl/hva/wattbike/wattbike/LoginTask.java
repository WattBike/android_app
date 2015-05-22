package nl.hva.wattbike.wattbike;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sean Molenaar
 * @since 1-5-15
 * @version 0.0.0.1
 */

/**
 * Download files from the main thread with this.
 *
 * @see android.os.AsyncTask
 */
public class LoginTask extends AsyncTask<String, Integer, String> {
    private TextView resultView;
    private String email, pass, UUID;

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
        } catch (IOException | NullPointerException e1) {
            e1.printStackTrace();
        }
        return output.toString();
    }

    /**
     * Code to query the server, only called by getOutputFromUrl()
     *
     * @param url the URL to query
     * @return bytestream version of the response
     * @throws IOException
     */
    private InputStream getHttpConnection(String url) throws IOException {
        InputStream stream = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("pass", pass));
            nameValuePairs.add(new BasicNameValuePair("UUID", UUID));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                stream = response.getEntity().getContent();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
            Log.i("failure", result);
            resultView.setText(result);
        }

    }

    public void setResultView(TextView t) {
        resultView = t;
    }

    public void setParams(String email, String pass, String uuid) {
        this.email = email;
        this.pass = pass;
        this.UUID = uuid;
    }
}
