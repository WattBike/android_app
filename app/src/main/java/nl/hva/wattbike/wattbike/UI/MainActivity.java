package nl.hva.wattbike.wattbike.UI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import nl.hva.wattbike.wattbike.HeartBeatTask;
import nl.hva.wattbike.wattbike.R;


public class MainActivity extends ActionBarActivity {
    TextView resultView;
    EditText inputText, inputEmail, inputPass;
    private String UUID, bpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        boolean loggedIn = sharedPref.getBoolean(getString(R.string.logged_in), false);
        setContentView(R.layout.activity_main);
        findViewsbyId();
        inputText.findFocus();
        if (loggedIn) {
            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            UUID = tManager.getDeviceId();
            bpm = "-1";
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        boolean loggedIn = sharedPref.getBoolean(getString(R.string.logged_in), false);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (loggedIn) {
            menu.removeItem(R.id.action_login);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        UUID = tManager.getDeviceId();
        bpm = "-1";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        Intent i = null;
        switch (id) {
            case R.id.action_settings:
                i = new Intent(this, DeviceScanActivity.class);
                break;
            case R.id.action_login:
                i = new Intent(this, LoginActivity.class);
                i.putExtra("logout", item.getTitle().equals(getString(R.string.menu_logout)));
                break;
        }
        startActivity(i);
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
     * What happens when you press the button in main.
     *
     * @param view Given by the system
     */
    public void sendRate(View view) {
        resultView.setText("...");
        resultView.setVisibility(View.GONE);

        bpm = inputText.getText().toString();
        String url = String.format("https://seanmolenaar.eu/team8/Application/rest.php?bpm=%s&UUID=%s", bpm, UUID);
        Log.i("URL", url);
        HeartBeatTask t = new HeartBeatTask();
        t.setResultView(resultView);
        t.execute(url);
    }


}