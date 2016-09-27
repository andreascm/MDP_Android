package mdp.mdp_android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import android.widget.ToggleButton;

public class MainActivity extends ActionBarActivity {
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_BLUETOOTH_DISCONNECT = 5;
    public static final int MESSAGE_BLUETOOTH_ERROR = 6;
    public static final int OBSTACLE_UPDATE = 7;
    public static final int DISPLAY_MOVEMENT_AND_STATUS = 8;
    public static final int STATUS_UPDATE = 9;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private String deviceName = null;
    private ArrayAdapter<String> messageArrayAdapter;
    private BluetoothAdapter bluetoothAdapter = null;
    private static Bluetooth bluetooth = null;
    private boolean bluetoothConnection = false;
    private static boolean autoMode = true;
    private String deviceAddress = null;
    private Handler reconnectHandler;
    private BluetoothDevice device;
    public int reconnectCount = 0;
    public static boolean secureConnection;

    private Map map;
    private Robot robot;
    private MapView mapView;
    private Joystick joystick;
    private View view;
    private LinearLayout mapGrid;
    private LinearLayout joystickLayout;

    private SharedPreferences sharedPreferences;

    private ImageButton mBluetoothButton;
    private ImageButton mForwardButton;
    private ImageButton mLeftButton;
    private ImageButton mRightButton;
    private ImageButton mBackButton;
    private ImageButton settingsbutton2;
    private Button f1Button;
    private Button f2Button;
    private Button startButton;
    private Button updateButton;
    private ToggleButton autoButton;
    private EditText mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            bluetoothConnection = savedInstanceState.getBoolean("bluetoothConnection");
            deviceAddress = savedInstanceState.getString("deviceAddress");
            deviceName = savedInstanceState.getString("deviceName");
        }
        sharedPreferences = getSharedPreferences("UserConfiguration",
                MODE_PRIVATE);
        f1Button = (Button) findViewById(R.id.f1button);
        f2Button = (Button) findViewById(R.id.f2button);

        f1Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String f1 = sharedPreferences.getString("f1", "");
                if (bluetooth != null) {
                    bluetooth.write(f1.getBytes());
                }
            }
        });

        f2Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String f2 = sharedPreferences.getString("f2", "");
                if (bluetooth != null) {
                    bluetooth.write(f2.getBytes());
                }
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothButton = (ImageButton) findViewById(R.id.bluetoothButton);

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
        
        settingsbutton2 = (ImageButton) findViewById(R.id.settingsButton2);
        settingsbutton2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, settingsbutton2);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bluetoothtesting:
                                Intent intent1 = new Intent(MainActivity.this, BluetoothTesting.class);
                                startActivity(intent1);
                                return true;
                            case R.id.startcoordinates:
                                Intent intent2 = new Intent(MainActivity.this, UpdateStartCoordinates.class);
                                startActivity(intent2);
                                return true;
                            case R.id.reconfigure:
                                Intent intent3 = new Intent(MainActivity.this, ReconfigureButtons.class);
                                startActivity(intent3);
                                return true;
                            default:
                                return true;
                        }

                    }
                });

                popup.show();//showing popup menu
            }
        });//closing the setOnClickListener method

        updateButton = (Button) findViewById(R.id.updatebutton);
        updateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!autoMode) {
                    mapView.invalidate();
                    mapGrid.removeAllViewsInLayout();
                    mapGrid.addView(mapView);
                    mapGrid.invalidate();
                }
            }
        });

        autoButton = (ToggleButton) findViewById(R.id.autoButton);
        autoButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoMode = !isChecked;
                Log.i("isChecked", String.valueOf(autoMode));
            }
        });

        startButton = (Button) findViewById(R.id.startbutton);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetooth != null) {
                    bluetooth.write("ps".getBytes());
                }
            }
        });

        mStatus = (EditText) findViewById(R.id.status);

        getSavedConfiguration();
        setupMap();

        mapGrid = (LinearLayout) findViewById(R.id.mapGrid);
        mapGrid.addView(mapView);
        mapGrid.invalidate();

        joystick = new Joystick(this);
        joystick.setOnJoystickMoveListener(new Joystick.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                Log.i("direction", String.valueOf(direction));
                Log.e("angle", String.valueOf(angle));
                if (power > 90) {
                    if (direction == Joystick.UP) {
                        String message = "hf";
                        if (bluetooth != null) {
                            bluetooth.write(message.getBytes());
                        }
                        robot.moveForward();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        if (autoMode) {
                            mapView.invalidate();
                            mapGrid.removeAllViewsInLayout();
                            mapGrid.addView(mapView);
                            mapGrid.invalidate();
                        }
                        mStatus.setText("Move Forward");
                    } else if (direction == Joystick.DOWN) {
                        String message = "hb";
                        if (bluetooth != null) {
                            bluetooth.write(message.getBytes());
                        }
                        robot.moveBackward();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        if (autoMode) {
                            mapView.invalidate();
                            mapGrid.removeAllViewsInLayout();
                            mapGrid.addView(mapView);
                            mapGrid.invalidate();
                        }
                        mStatus.setText("Move Bakcward");
                    } else if (direction == Joystick.LEFT) {
                        String message = "hl";
                        if (bluetooth != null) {
                            bluetooth.write(message.getBytes());
                        }
                        robot.turnLeft();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        if (autoMode) {
                            mapView.invalidate();
                            mapGrid.removeAllViewsInLayout();
                            mapGrid.addView(mapView);
                            mapGrid.invalidate();
                        }
                        mStatus.setText("Turn Left");
                    } else if (direction == Joystick.RIGHT) {
                        String message = "hr";
                        if (bluetooth != null) {
                            bluetooth.write(message.getBytes());
                        }
                        robot.turnRight();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        if (autoMode) {
                            mapView.invalidate();
                            mapGrid.removeAllViewsInLayout();
                            mapGrid.addView(mapView);
                            mapGrid.invalidate();
                        }
                        mStatus.setText("Turn Right");
                    }
                }
            }
        }, 500);
        joystickLayout = (LinearLayout) findViewById(R.id.joystickLayout);
        joystickLayout.addView(joystick);

        mForwardButton = (ImageButton) findViewById(R.id.arrowUp);
        mLeftButton = (ImageButton) findViewById(R.id.arrowLeft);
        mRightButton = (ImageButton) findViewById(R.id.arrowRight);
        mBackButton = (ImageButton) findViewById(R.id.arrowDown);

        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "hf";
                if (bluetooth != null) {
                    bluetooth.write(message.getBytes());
                }
                robot.moveForward();
                map = robot.discoverSurrounding();
                mapView.setCurrentX(robot.getCurrentX());
                mapView.setCurrentY(robot.getCurrentY());
                mapView.setDirection(robot.getDirection());
                mapView.updatePainted(map.getMapData());
                if (autoMode) {
                    mapView.invalidate();
                    mapGrid.removeAllViewsInLayout();
                    mapGrid.addView(mapView);
                    mapGrid.invalidate();
                }
                mStatus.setText("Move Forward");
            }
        });

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "hl";
                if (bluetooth != null) {
                    bluetooth.write(message.getBytes());
                }
                robot.turnLeft();
                map = robot.discoverSurrounding();
                mapView.setCurrentX(robot.getCurrentX());
                mapView.setCurrentY(robot.getCurrentY());
                mapView.setDirection(robot.getDirection());
                mapView.updatePainted(map.getMapData());
                if (autoMode) {
                    mapView.invalidate();
                    mapGrid.removeAllViewsInLayout();
                    mapGrid.addView(mapView);
                    mapGrid.invalidate();
                }
                mStatus.setText("Turn Left");
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "hr";
                if (bluetooth != null) {
                    bluetooth.write(message.getBytes());
                }
                robot.turnRight();
                map = robot.discoverSurrounding();
                mapView.setCurrentX(robot.getCurrentX());
                mapView.setCurrentY(robot.getCurrentY());
                mapView.setDirection(robot.getDirection());
                mapView.updatePainted(map.getMapData());
                if (autoMode) {
                    mapView.invalidate();
                    mapGrid.removeAllViewsInLayout();
                    mapGrid.addView(mapView);
                    mapGrid.invalidate();
                }
                mStatus.setText("Turn Right");
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "hb";
                if (bluetooth != null) {
                    bluetooth.write(message.getBytes());
                }
                robot.moveBackward();
                map = robot.discoverSurrounding();
                mapView.setCurrentX(robot.getCurrentX());
                mapView.setCurrentY(robot.getCurrentY());
                mapView.setDirection(robot.getDirection());
                mapView.updatePainted(map.getMapData());
                if (autoMode) {
                    mapView.invalidate();
                    mapGrid.removeAllViewsInLayout();
                    mapGrid.addView(mapView);
                    mapGrid.invalidate();
                }
                mStatus.setText("Move Bakcward");
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
                case DISPLAY_MOVEMENT_AND_STATUS:
                    String received = (String) msg.obj;
                    Log.i("receive", received);
                    if (received.charAt(0) == 'f') {
                        robot.moveForward();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        if (autoMode) {
                            mapView.invalidate();
                            mapGrid.removeAllViewsInLayout();
                            mapGrid.addView(mapView);
                            mapGrid.invalidate();
                        }
                        mStatus.setText("Move Forward");
                    } else if (received.charAt(0) == 'b') {
                        robot.moveBackward();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        if (autoMode) {
                            mapView.invalidate();
                            mapGrid.removeAllViewsInLayout();
                            mapGrid.addView(mapView);
                            mapGrid.invalidate();
                        }
                        mStatus.setText("Move Backward");
                    } else if (received.charAt(0) == 'l') {
                        robot.turnLeft();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        if (autoMode) {
                            mapView.invalidate();
                            mapGrid.removeAllViewsInLayout();
                            mapGrid.addView(mapView);
                            mapGrid.invalidate();
                        }
                        mStatus.setText("Turn Left");
                    } else if (received.charAt(0) == 'r') {
                        robot.turnRight();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        if (autoMode) {
                            mapView.invalidate();
                            mapGrid.removeAllViewsInLayout();
                            mapGrid.addView(mapView);
                            mapGrid.invalidate();
                        }
                        mStatus.setText("Turn Right");
                    }
                    break;
                case OBSTACLE_UPDATE:
                    String message = (String) msg.obj;
                    int obstacleX, obstacleY;
                    int[] sensor = new int[5];
                    for (int i=0; i<5; i++) {
                        sensor[i] = Integer.parseInt(message.substring(i, i+1));
                    }
                    if (sensor[0] > 0 && sensor[0] < 4) {
                        if (robot.getDirection() == Robot.UP) {
                            obstacleX = robot.getCurrentX() - 1;
                            obstacleY = robot.getCurrentY() + sensor[0];
                        } else if (robot.getDirection() == Robot.DOWN) {
                            obstacleX = robot.getCurrentX() + 1;
                            obstacleY = robot.getCurrentY() - sensor[0];
                        } else if (robot.getDirection() == Robot.LEFT) {
                            obstacleX = robot.getCurrentX() - sensor[0];
                            obstacleY = robot.getCurrentY() - 1;
                        } else {
                            obstacleX = robot.getCurrentX() + sensor[0];
                            obstacleY = robot.getCurrentY() + 1;
                        }
                        map.setDiscovered(obstacleX, obstacleY);
                    }
                    if (sensor[1] > 0 && sensor[1] < 4) {
                        if (robot.getDirection() == Robot.UP) {
                            obstacleX = robot.getCurrentX();
                            obstacleY = robot.getCurrentY() + sensor[0];
                        } else if (robot.getDirection() == Robot.DOWN) {
                            obstacleX = robot.getCurrentX();
                            obstacleY = robot.getCurrentY() - sensor[0];
                        } else if (robot.getDirection() == Robot.LEFT) {
                            obstacleX = robot.getCurrentX() - sensor[0];
                            obstacleY = robot.getCurrentY();
                        } else {
                            obstacleX = robot.getCurrentX() + sensor[0];
                            obstacleY = robot.getCurrentY();
                        }
                        map.setObstacle(obstacleX, obstacleY);
                    }
                    if (sensor[2] > 0 && sensor[2] < 4) {
                        if (robot.getDirection() == Robot.UP) {
                            obstacleX = robot.getCurrentX() + 1;
                            obstacleY = robot.getCurrentY() + sensor[0];
                        } else if (robot.getDirection() == Robot.DOWN) {
                            obstacleX = robot.getCurrentX() - 1;
                            obstacleY = robot.getCurrentY() - sensor[0];
                        } else if (robot.getDirection() == Robot.LEFT) {
                            obstacleX = robot.getCurrentX() - sensor[0];
                            obstacleY = robot.getCurrentY() + 1;
                        } else {
                            obstacleX = robot.getCurrentX() + sensor[0];
                            obstacleY = robot.getCurrentY() - 1;
                        }
                        map.setObstacle(obstacleX, obstacleY);
                    }
                    if (sensor[3] > 0 && sensor[3] < 4) {
                        if (robot.getDirection() == Robot.UP) {
                            obstacleX = robot.getCurrentX() - sensor[0];
                            obstacleY = robot.getCurrentY();
                        } else if (robot.getDirection() == Robot.DOWN) {
                            obstacleX = robot.getCurrentX() + sensor[0];
                            obstacleY = robot.getCurrentY();
                        } else if (robot.getDirection() == Robot.LEFT) {
                            obstacleX = robot.getCurrentX();
                            obstacleY = robot.getCurrentY() - sensor[0];
                        } else {
                            obstacleX = robot.getCurrentX();
                            obstacleY = robot.getCurrentY() + sensor[0];
                        }
                        map.setObstacle(obstacleX, obstacleY);
                    }
                    if (sensor[4] > 0 && sensor[4] < 4) {
                        if (robot.getDirection() == Robot.UP) {
                            obstacleX = robot.getCurrentX() + sensor[0];
                            obstacleY = robot.getCurrentY();
                        } else if (robot.getDirection() == Robot.DOWN) {
                            obstacleX = robot.getCurrentX() - sensor[0];
                            obstacleY = robot.getCurrentY();
                        } else if (robot.getDirection() == Robot.LEFT) {
                            obstacleX = robot.getCurrentX();
                            obstacleY = robot.getCurrentY() + sensor[0];
                        } else {
                            obstacleX = robot.getCurrentX();
                            obstacleY = robot.getCurrentY() - sensor[0];
                        }
                        map.setObstacle(obstacleX, obstacleY);
                    }
                    mapView.updatePainted(map.getMapData());
                    if (autoMode) {
                        mapView.invalidate();
                        mapGrid.removeAllViewsInLayout();
                        mapGrid.addView(mapView);
                        mapGrid.invalidate();
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

    private void getSavedConfiguration() {
        sharedPreferences = getSharedPreferences("SavedConfiguration", MODE_PRIVATE);
    }

    private void setupMap() {

        map = new Map(0, 0);
        map.resetMap();

        robot = new Robot(map, 1, 1, Robot.RIGHT);
        map = robot.discoverSurrounding();

        for (int i=0; i<15; i++) {
            for (int j=0; j<20; j++) {
                if (map.getMapData()[i][j] == 1) {
                }
            }
        }

        mapView = new MapView(this, map.getMapData(), robot.getCurrentX(), robot.getCurrentY(), robot.getDirection());
        mapView.invalidate();
    }

    public static Bluetooth getBluetooth() {
        return bluetooth;
    }
}
