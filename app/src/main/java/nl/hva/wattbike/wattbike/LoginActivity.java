package nl.hva.wattbike.wattbike;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
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


public class LoginActivity extends ActionBarActivity {

    TextView resultView;
    private EditText inputEmail, inputPass;
    private String UUID;
    private SharedPreferences sharedPref;
    private boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Getting device ID to use in the registry,
        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        UUID = tManager.getDeviceId();
        //Getting email from settings
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.saved_email_default);
        String email = sharedPref.getString(getString(R.string.saved_email), defaultValue);
        loggedIn = sharedPref.getBoolean(getString(R.string.logged_in), false);
        //Setting UI part
        setContentView(R.layout.activity_login);
        findViewsbyId();
        boolean logoutTriggered = getIntent().getBooleanExtra("logout", false);
        if (email != null) {
            inputEmail.setText((email.equals(defaultValue)) ? "" : email);
        }
        if (!logoutTriggered && loggedIn) {
            finish();
        } else {
            setLoggedIn(false);
        }
        inputEmail.findFocus();
    }

    private void findViewsbyId() {
        inputEmail = (EditText) findViewById(R.id.editEmail);
        inputPass = (EditText) findViewById(R.id.editPassword);
        resultView = (TextView) findViewById(R.id.textLoginResult);
    }

    /**
     * When the login button is pressed, this is triggered
     *
     * @param view Given by the system
     */
    public void queryLogin(View view) {
        String email = inputEmail.getText().toString();
        String pass = inputPass.getText().toString();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_email), email);
        editor.commit();
        LoginTask t = new LoginTask();
        t.setParams(email, pass, UUID);
        t.execute("https://seanmolenaar.eu/team8/Application/rest.php");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                break;
            default:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.logged_in), loggedIn);
        editor.commit();
        this.loggedIn = loggedIn;
        if (loggedIn) {
            finish();
        }
    }

    /**
     * Download files from the main thread with this.
     *
     * @see android.os.AsyncTask
     */
    private class LoginTask extends AsyncTask<String, Integer, String> {
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
                BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
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
         */
        private InputStream getHttpConnection(String url) {
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
                //if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                stream = response.getEntity().getContent();
                //}
            } catch (IOException e) {
                e.printStackTrace();
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
                setLoggedIn(status.equals("login"));
            } catch (JSONException e) {
                resultView.setText(result);
            }
            Log.i("HTTPlogin", "Result= " + result);
        }

        public void setParams(String email, String pass, String uuid) {
            this.email = email;
            this.pass = pass;
            this.UUID = uuid;
        }
    }

}
