package mdp.mdp_android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_BLUETOOTH_DISCONNECT = 5;
    public static final int MESSAGE_BLUETOOTH_ERROR = 6;
    public static final int GRID_UPDATE = 7;
    public static final int DISPLAY_MOVEMENT_AND_STATUS = 8;
    public static final int STATUS_UPDATE = 9;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int CONFIG_SETTING = 3;
    private static final int CUSTOM_COMMAND = 4;
    private static final int LoadDefaultCoordinates = 5;

    // Modes
    private static final int MODE_EXPLORATION = 1;
    private static final int MODE_RACE = 2;
    private static final int MODE_MANUAL = 3;

    // Name of the connected device
    private String deviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> messageArrayAdapter;
    // Local Bluetooth adapter
    private BluetoothAdapter bluetoothAdapter = null;
    // Member object for the chat services
    private Bluetooth bluetooth = null;

    private boolean bluetoothConnection = false;
    private boolean isAutoRefresh = true;

    private String deviceAddress = null;

    private Handler reconnectHandler; //For handling reconnection request.
    private BluetoothDevice device;

    public int reconnectCount = 0;
    public static boolean secureConnection; //Whether connection should be done in secure mode or not.

    private Button mBluetoothButton;
    private Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            bluetoothConnection = savedInstanceState.getBoolean("bluetoothConnection");
            deviceAddress = savedInstanceState.getString("deviceAddress");
            deviceName = savedInstanceState.getString("deviceName");
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothButton = (Button) findViewById(R.id.bluetooth_button);
        mSendButton = (Button) findViewById(R.id.send_button);

        mBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isEnabled()) {
                    setupBluetoothConnection();
                } else {
                    Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
                }
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetooth.getState() == Bluetooth.STATE_CONNECTED) {
                    String message = "message";
                    bluetooth.write(message.getBytes());
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("bluetoothConnection", bluetoothConnection);
        outState.putString("deviceAddress", deviceAddress);
        outState.putString("deviceName", deviceName);
    }

    private void setupBluetoothConnection() {
        messageArrayAdapter = new ArrayAdapter<String>(this, R.layout.message_layout);
        bluetooth = new Bluetooth(this, handler);
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Bluetooth.STATE_CONNECTED:
                            bluetoothConnection = true;
                            messageArrayAdapter.clear();
                            reconnectCount = 0;
                            break;

                        case Bluetooth.STATE_CONNECTING:
                            break;

                        case Bluetooth.STATE_LISTEN:
                            break;

                        case Bluetooth.STATE_NONE:
                            break;
                    }
                    break;

                case MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuffer, 0, msg.arg1);

                    //Add into adapter that contains list of connected device.
                    messageArrayAdapter.add(deviceName + ":  "
                            + readMessage);
                    break;

                case MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuffer);
                    messageArrayAdapter.add("Me:  " + writeMessage);
                    break;

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    deviceName = msg.getData().getString(
                            Bluetooth.DEVICE_NAME);
                    break;

                case MESSAGE_BLUETOOTH_DISCONNECT:
                    bluetoothConnection = false; //State must be disconnected as requested / not within reception range.
                    break;

                case MESSAGE_BLUETOOTH_ERROR:
                    bluetoothConnection = false; //State must be disconnected when connection has error.
                    if (deviceName != null) {
                        try {
                            reconnect(); //reconnect will handle should next attempt be made or not.
                        } catch (InterruptedException e) {
                        }
                    }
                    else {
                        bluetooth.stop();
                    }
                    break;
                case -1:
                    break;
            }
        }
    };

    public void reconnect() throws InterruptedException {
        reconnectCount++; //Increase it for every reconnect instance.

        if (reconnectCount < 6)
        {
            reconnectHandler = new Handler();
            reconnectHandler.postDelayed(rerun, 5000);

        }
        else {
            reconnectCount = 0; //Reset reconnectCount.
            bluetooth.stop();
        }

    }

    Runnable rerun = new Runnable() {

        @Override
        public void run() {
            if (bluetooth.getState() != Bluetooth.STATE_CONNECTED) {
                device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                Log.i("Connecting to: ", deviceAddress);
                bluetooth.connect(device,secureConnection);
            }
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    deviceAddress = data.getExtras().getString(
                            BluetoothList.DEVICE_ADDRESS);
                    // Get the BluetoothDevice object
                    device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    // Attempt to connect to the device
                    bluetooth.connect(device,secureConnection);
                }
                break;

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupBluetoothConnection();
                    //Show select device to connect menu.
                    Intent serverIntent = new Intent(this, BluetoothList.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                } else {
                }
                break;
        }
    }
}