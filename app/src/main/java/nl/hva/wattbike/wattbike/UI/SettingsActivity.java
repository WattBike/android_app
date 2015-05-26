package nl.hva.wattbike.wattbike.UI;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Set;

import nl.hva.wattbike.wattbike.R;

/**
 * Created by Sean on 26/05/2015.
 * TODO Make more settings and fix BT
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SettingsActivity extends ActionBarActivity {


    private static final int REQUEST_ENABLE_BT = 45621;
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning;
    private Button toggleBtn, listBtn, findBtn;
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<BluetoothDevice> BTArrayAdapter;
    //PAIR BLUETOOTH DEVICES
    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                    BTArrayAdapter.add(device);
                    BTArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };
    private BluetoothLeScanner bluetoothLeScanner;
    // Device scan callback.
    private ScanCallback mLeScanCallback =
            new ScanCallback() {
                @Override
                public void onBatchScanResults(final List<ScanResult> results) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < results.size(); i++) {
                                ScanResult r = results.get(i);
                                BluetoothDevice device = r.getDevice();
                                BTArrayAdapter.add(device);
                            }
                        }
                    });
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        text = (TextView) findViewById(R.id.bluetooth_header);
        listBtn = (Button) findViewById(R.id.paired);
        findBtn = (Button) findViewById(R.id.search);
        toggleBtn = (Button) findViewById(R.id.toggle_bt);
        myListView = (ListView) findViewById(R.id.listView1);
        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toggleBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Bluetooth Status: not supported");
            Toast.makeText(getApplicationContext(), R.string.ble_not_supported, Toast.LENGTH_LONG).show();
        } else {
            //enable the app to scan for bluetoothLE devices
            bluetoothLeScanner = myBluetoothAdapter.getBluetoothLeScanner();

            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            myListView.setAdapter(BTArrayAdapter);
            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    BluetoothDevice device = BTArrayAdapter.getItem(position);
                    String item = "Device = " + device.getName() + "\n Class = " + device.getType();
                    Toast.makeText(getBaseContext(), item, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (myBluetoothAdapter.isEnabled()) {
                text.setText("Status: Enabled");
            } else {
                text.setText("Status: Disabled");
            }
        }
    }

    public void toggle(View view) {
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(), "Bluetooth turned on",
                    Toast.LENGTH_LONG).show();
        } else {
            myBluetoothAdapter.disable();
            text.setText("Status: Disconnected");
            Toast.makeText(getApplicationContext(), "Bluetooth turned off",
                    Toast.LENGTH_LONG).show();
        }
        setToggleText();
    }

    private void setToggleText() {
        if (!myBluetoothAdapter.isEnabled()) {
            toggleBtn.setText(R.string.bluetooth_off);
        } else {
            toggleBtn.setText(R.string.bluetooth_on);
        }
    }

    //LOOKS FOR DEVICES
    public void find(View view) {
        if (myBluetoothAdapter.isDiscovering()) {
            mScanning = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);
        } else {
            // Stops scanning after a pre-defined scan period.
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            BTArrayAdapter.clear();
            bluetoothLeScanner.startScan(mLeScanCallback);
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }
}
