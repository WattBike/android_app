package nl.hva.wattbike.wattbike;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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


public class MainActivity extends ActionBarActivity {
    TextView resultView;
    EditText inputText;
    private String UUID, bpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        UUID = tManager.getDeviceId();
        bpm = "12";
        setContentView(R.layout.activity_main);
        findViewsbyId();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            onCreateSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set variables for cross class editing
     */
    private void findViewsbyId() {
        resultView = (TextView) findViewById(R.id.textResult);
        inputText = (EditText) findViewById(R.id.editText);
    }

    /**
     * What happens when you go to settings
     */
    protected void onCreateSettings() {
        setContentView(R.layout.activity_settings);
    }

    /**
     * What happens when you press the button in main.
     *
     * @param view Given by the system
     */
    public void sendRate(View view) {
        resultView.setText("...");
        resultView.setVisibility(View.GONE);

        bpm = inputText.getText().toString();
        String url = String.format("http://seanmolenaar.eu/team8/Application/rest.php?bpm=%s&UUID=%s", bpm, UUID);

        DownloadFilesTask t= new DownloadFilesTask();
        t.execute(url);
    }

    /**
     * @param view Given by the system
     */
    public void queryLogin(View view) {

    }


    /**
     * Download files from the main thread with this.
     *
     * @see android.os.AsyncTask
     */
    private class DownloadFilesTask extends AsyncTask<String, Integer, String> {
        /**
         * Executed in background. Called by .execute(<String>)
         *
         * @param urls the urls you want to query
         * @return Output of the query
         */
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }


        /**
         * Code to read the response of the server
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
            String status = "error";
            try {
                JSONObject object = new JSONObject(result);
                status = object.getString("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            resultView.setText(status);
        }
    }
}