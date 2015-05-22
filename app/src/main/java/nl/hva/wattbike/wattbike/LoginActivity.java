package nl.hva.wattbike.wattbike;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class LoginActivity extends ActionBarActivity {

    EditText inputEmail, inputPass;
    TextView resultView;
    String UUID;
    SharedPreferences sharedPref;

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

        //Setting UI part
        setContentView(R.layout.activity_login);
        findViewsbyId();
        if (email != null) {
            inputEmail.setText((email.equals(defaultValue)) ? "" : email);
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
        t.setResultView(resultView);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
