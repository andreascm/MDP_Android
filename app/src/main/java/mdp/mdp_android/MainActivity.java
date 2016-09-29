package mdp.mdp_android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends ActionBarActivity implements SensorEventListener {
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_BLUETOOTH_DISCONNECT = 5;
    public static final int MESSAGE_BLUETOOTH_ERROR = 6;
    public static final int OBSTACLE_UPDATE = 7;
    public static final int DISPLAY_MOVEMENT_AND_STATUS = 8;
    public static final int STATUS_UPDATE = 9;
    public static final int GRID_UPDATE = 10;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UPDATE_START = 3;
    private static final int UPDATE_CONFIG = 4;

    private String deviceName = null;
    private ArrayAdapter<String> messageArrayAdapter;
    private BluetoothAdapter bluetoothAdapter = null;
    private static Bluetooth bluetooth = null;
    private boolean bluetoothConnection = false;
    private boolean autoMode = true;
    private String deviceAddress = null;
    private Handler reconnectHandler;
    private BluetoothDevice device;
    public int reconnectCount = 0;
    public static boolean secureConnection;

    private Map map;
    private Robot robot;
    private MapView mapView;
    private Joystick joystick;
    private LinearLayout mapGrid;
    private LinearLayout joystickLayout;

    private SharedPreferences sharedPreferences;

    private ImageButton mBluetoothButton;
    private ImageButton mForwardButton;
    private ImageButton mLeftButton;
    private ImageButton mRightButton;
    private ImageButton mBackButton;
    private ImageButton settingsbutton2;
    private ImageButton settingsbutton;
    private ToggleButton autoButton;
    private Button updateButton;
    private Button f1Button;
    private Button f2Button;
    private Button startButton;
    private EditText mStatus;

    private boolean tiltMode= false;
    private boolean joystickMode = false;
    private int pre_state = 0;
    private String f1;
    private String f2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            bluetoothConnection = savedInstanceState.getBoolean("bluetoothConnection");
            deviceAddress = savedInstanceState.getString("deviceAddress");
            deviceName = savedInstanceState.getString("deviceName");
        }

        sharedPreferences = getSharedPreferences("UserConfiguration", MODE_PRIVATE);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        f1 = sharedPreferences.getString("f1", "");
        f2 = sharedPreferences.getString("f2", "");

        f1Button = (Button) findViewById(R.id.f1button);
        f2Button = (Button) findViewById(R.id.f2button);

        f1Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetooth != null) {
                    bluetooth.write(f1.getBytes());
                }
            }
        });

        f2Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
                setupBluetoothConnection();
            }
        });
        settingsbutton = (ImageButton) findViewById(R.id.settingsButton);
        settingsbutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup2 = new PopupMenu(MainActivity.this, settingsbutton);
                popup2.getMenuInflater().inflate(R.menu.popup_menu2, popup2.getMenu());
                popup2.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.tiltmode:
                                /* tilt mode */
                                if (tiltMode) {
                                    Toast.makeText(getApplicationContext(), R.string.Tilt_Off,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.Tilt_On,
                                            Toast.LENGTH_SHORT).show();
                                    joystickMode = false;
                                    joystickLayout.removeAllViewsInLayout();
                                    joystickLayout.setVisibility(View.GONE);
                                    joystickLayout.invalidate();
                                }

                                tiltMode = !tiltMode; //Toggle the tilt mode settings.

                                invalidateOptionsMenu();
                                return true;
                            case R.id.joystick:
                                /* joystick mode */
                                if (joystickMode) {
                                    Toast.makeText(getApplicationContext(), R.string.Joystick_Off,
                                            Toast.LENGTH_SHORT).show();
                                    joystickLayout.removeAllViewsInLayout();
                                    joystickLayout.setVisibility(View.GONE);
                                    joystickLayout.invalidate();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.Joystick_On,
                                            Toast.LENGTH_SHORT).show();
                                    tiltMode = false;
                                    joystickLayout.addView(joystick);
                                    joystickLayout.setVisibility(View.VISIBLE);
                                    joystickLayout.invalidate();
                                }

                                joystickMode = !joystickMode; //Toggle the tilt mode settings.

                                invalidateOptionsMenu();
                                return true;
                            default:
                                return true;
                        }
                    }
                });
                popup2.show();
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
                                startActivityForResult(intent2, UPDATE_START);
                                return true;
                            case R.id.reconfigure:
                                Intent intent3 = new Intent(MainActivity.this, ReconfigureButtons.class);
                                startActivityForResult(intent3, UPDATE_CONFIG);
                                return true;
                            default:
                                return true;
                        }

                    }
                });

                popup.show();//showing popup menu
            }
        });//closing the setOnClickListener method

        autoButton = (ToggleButton) findViewById(R.id.autoButton);
        autoButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoMode = !isChecked;
            }
        });

        updateButton = (Button) findViewById(R.id.updatebutton);
        updateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.invalidate();
                mapGrid.removeAllViewsInLayout();
                mapGrid.addView(mapView);
                mapGrid.invalidate();
            }
        });

        startButton = (Button) findViewById(R.id.startbutton);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetooth != null) {
                    bluetooth.write("s".getBytes());
                }
            }
        });

        mStatus = (EditText) findViewById(R.id.status);

        setupMap();

        mapGrid = (LinearLayout) findViewById(R.id.mapGrid);
        mapGrid.addView(mapView);
        mapGrid.invalidate();

        joystick = new Joystick(this);
        joystick.setOnJoystickMoveListener(new Joystick.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
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
        }, 350);
        joystickLayout = (LinearLayout) findViewById(R.id.joystickLayout);

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
                    mStatus.setText("Move Forward");
                }
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
                    mStatus.setText("Turn Left");
                }
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
                    mStatus.setText("Turn Right");
                }
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
                    mStatus.setText("Move Bakcward");
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
                    Toast.makeText(MainActivity.this, "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
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
                    if (received.charAt(0) == 'f') {
                        robot.moveForward();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        mapView.invalidate();
                        mStatus.setText("Move Forward");
                    } else if (received.charAt(0) == 'b') {
                        robot.moveBackward();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        mapView.invalidate();
                        mStatus.setText("Move Backward");
                    } else if (received.charAt(0) == 'l') {
                        robot.turnLeft();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        mapView.invalidate();
                        mStatus.setText("Turn Left");
                    } else if (received.charAt(0) == 'r') {
                        robot.turnRight();
                        map = robot.discoverSurrounding();
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        mapView.invalidate();
                        mStatus.setText("Turn Right");
                    } else if (Character.isDigit(received.charAt(0))) {
                        int numStep = Integer.parseInt(received.substring(0, 1));
                        for (int i=0; i<numStep; i++) {
                            robot.moveForward();
                            map = robot.discoverSurrounding();
                        }
                        mapView.setCurrentX(robot.getCurrentX());
                        mapView.setCurrentY(robot.getCurrentY());
                        mapView.setDirection(robot.getDirection());
                        mapView.updatePainted(map.getMapData());
                        mapView.invalidate();
                        mStatus.setText("Move Forward");
                    }
                    break;
                case OBSTACLE_UPDATE:
                    String update = (String) msg.obj;
                    int obstacleX, obstacleY;
                    int[] sensor = new int[5];
                    for (int i=0; i<5; i++) {
                        sensor[i] = Integer.parseInt(update.substring(i, i+1));
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
                        map.setObstacle(obstacleX, obstacleY);
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
                case GRID_UPDATE:
                    String grid = (String) msg.obj;

                    int[] gridInt = new int[75];
                    int[] gridArray = new int[300];

                    for (int i=0; i<75; i++) {
                        if (Character.isDigit(grid.charAt(i))) {
                            gridInt[i] = Integer.parseInt(grid.substring(i, i + 1));
                        } else {
                            String temp = grid.substring(i, i+1);
                            switch (temp) {
                                case "a":
                                    gridInt[i] = 10;
                                    break;
                                case "b":
                                    gridInt[i] = 11;
                                    break;
                                case "c":
                                    gridInt[i] = 12;
                                    break;
                                case "d":
                                    gridInt[i] = 13;
                                    break;
                                case "e":
                                    gridInt[i] = 14;
                                    break;
                                case "f":
                                    gridInt[i] = 15;
                                    break;
                            }
                        }
                    }

                    for (int i=0; i<300; i++) {
                        String temp = String.format("%4s", Integer.toBinaryString(gridInt[(int) (i/4)])).replace(' ', '0');
                        Log.i("gridArray " + i/4, String.valueOf(gridInt[(int) (i/4)]));
                        Log.i("temp " + i, temp);
                        gridArray[i] = Integer.parseInt(temp.substring(i%4, (i%4)+1));
                    }

                    for (int j=0; j<20; j++) {
                        for (int i=0; i<15; i++) {
                            if (gridArray[j*15+i] == 1) {
                                map.setObstacle(i, 19-j);
                            }
                        }
                    }

                    mapView.updatePainted(map.getMapData());
                    if (autoMode) {
                        mapGrid.removeAllViewsInLayout();
                        mapGrid.addView(mapView);
                        mapGrid.invalidate();
                    }

                default:
                    String message = (String) msg.obj;
                    Log.i("message", message);
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

            case UPDATE_START:
                if (resultCode == Activity.RESULT_OK) {

                    map = new Map(0, 0);
                    map.resetMap();

                    robot = new Robot(map, sharedPreferences.getInt("xpos", 1), sharedPreferences.getInt("ypos", 1), Robot.UP);
                    map = robot.discoverSurrounding();

                    mapView = new MapView(this, map.getMapData(), robot.getCurrentX(), robot.getCurrentY(), robot.getDirection());
                    mapGrid.removeAllViewsInLayout();
                    mapGrid.addView(mapView);
                    mapGrid.invalidate();
                }
                break;

            case UPDATE_CONFIG:
                if (resultCode == Activity.RESULT_OK) {

                    f1 = sharedPreferences.getString("f1", "");
                    f2 = sharedPreferences.getString("f2", "");
                }
                break;
        }
    }

    private void setupMap() {

        map = new Map(0, 0);
        map.resetMap();

        robot = new Robot(map, 1, 1, Robot.UP);
        map = robot.discoverSurrounding();

        mapView = new MapView(this, map.getMapData(), robot.getCurrentX(), robot.getCurrentY(), robot.getDirection());
        mapView.invalidate();
    }

    public static Bluetooth getBluetooth() {
        return bluetooth;
    }

    public void tiltTurnRight()
    {
        String message = "hr";
        if (bluetooth != null) {
            bluetooth.write(message.getBytes());
        }
        if (autoMode) {
            robot.turnRight();
        }
    }

    public void tiltTurnLeft()
    {
        String message = "hl";
        if (bluetooth != null) {
            bluetooth.write(message.getBytes());
        }
        if (autoMode) {
            robot.turnLeft();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // check sensor type
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // assign directions
            float x = event.values[0];
            float y = event.values[1];

            if (tiltMode) {

                if (x < -1) {
                    //When x is negative value, it is tilting towards RIGHT.
                    if (x > -4) {
                        pre_state = 0; //Register position as centered.

                    } else if (x <= -4) {
                        //When x is <= -4, it is expected to turn right.
                        if (pre_state != 2) {
                            //Tilt right IF previously the state is NOT tilted towards right.
                            pre_state = 2;
                            tiltTurnRight();
                        }
                    }
                } else if (x > 1) {
                    //When x is a positive value, it is tilting towards LEFT.
                    if (x < 4) {
                        pre_state = 0;
                    } else if (x >= 4) {
                        //When x is >= 4, it is expected to turn left.
                        if (pre_state != 1) {
                            //Tilt left IF previously the state is NOT tilted towards left.
                            pre_state = 1;
                            tiltTurnLeft();
                        }
                    }
                } else if (y > 3) {
                    //If y is more than 3 of first held offset, it is expected to reverse.
                    pre_state = 0;
                    String message = "hb";
                    if (bluetooth != null) {
                        bluetooth.write(message.getBytes());
                    }
                    if (autoMode) {
                        robot.moveBackward();
                    }
                } else if (y < -0.1) {
                    //Robots moves at a very slight of tilting forward.
                    pre_state = 0;
                    String message = "hf";
                    if (bluetooth != null) {
                        bluetooth.write(message.getBytes());
                    }
                    if (autoMode) {
                        robot.moveForward();
                    }

                }
                map = robot.discoverSurrounding();
                mapView.setCurrentX(robot.getCurrentX());
                mapView.setCurrentY(robot.getCurrentY());
                mapView.setDirection(robot.getDirection());
                mapView.updatePainted(map.getMapData());
                mapView.invalidate();
                mapGrid.removeAllViewsInLayout();
                mapGrid.addView(mapView);
                mapGrid.invalidate();

            }
        }

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }
}
