/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mdp.mdp_android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class BluetoothList extends ActionBarActivity {
    // Return Intent extra
    public static String DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> newDeviceArrayAdapter;

    private Button mScanButton;
    private ArrayAdapter<String> pairedDeviceArrayAdapter;
    private ArrayAdapter<String> nearbyDeviceArrayAdapter;
    private ListView pairedDeviceListView;
    private ListView nearbyDeviceListView;

    private Set<BluetoothDevice> pairedDeviceSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        setResult(Activity.RESULT_CANCELED);

        mScanButton = (Button) findViewById(R.id.scan_button);

        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanNearbyDevices();
                v.setVisibility(View.GONE);
            }
        });

        pairedDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.message_layout);
        nearbyDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.message_layout);

        pairedDeviceListView = (ListView) findViewById(R.id.paired_device_list);
        pairedDeviceListView.setAdapter(pairedDeviceArrayAdapter);
        pairedDeviceListView.setOnItemClickListener(deviceOnClickListener);

        nearbyDeviceListView = (ListView) findViewById(R.id.nearby_device_list);
        nearbyDeviceListView.setAdapter(nearbyDeviceArrayAdapter);
        nearbyDeviceListView.setOnItemClickListener(deviceOnClickListener);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, intentFilter);
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, intentFilter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDeviceSet = bluetoothAdapter.getBondedDevices();
        if (pairedDeviceSet.size() > 0) {
            for (BluetoothDevice bluetoothDevice : pairedDeviceSet) {
                pairedDeviceArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
            }
        } else {
            pairedDeviceArrayAdapter.add("No paired device");
        }
    }

    private void scanNearbyDevices() {
        setProgressBarIndeterminateVisibility(true);
        setTitle("Scanning for nearby devices");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed
                // already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    nearbyDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle("Select device");
                if (nearbyDeviceArrayAdapter.getCount() == 0) {
                    nearbyDeviceArrayAdapter.add("No devices found");
                }
            }
        }
    };

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener deviceOnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            bluetoothAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the
            // View
            String deviceDetails = ((TextView) v).getText().toString();
            String deviceMAC = deviceDetails.substring(deviceDetails.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(DEVICE_ADDRESS, deviceMAC);

            //Configure secureConnection mode
            CheckBox mSecureCheckBox = (CheckBox) findViewById(R.id.secure_check_box);
            MainActivity.secureConnection = mSecureCheckBox.isChecked();

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
}
