

package mdp.mdp_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by andreaschrisnamayong on 9/8/16.
 */
public class Bluetooth {
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private AcceptThread secureAcceptThread;
    private AcceptThread insecureAcceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private int currentState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0; //Now doing nothing
    public static final int STATE_LISTEN = 1; //Now listening for incoming connections
    public static final int STATE_CONNECTING = 2; //Now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3; //Now connected to a remote device

    // Message types sent from the Service Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_BLUETOOTH_DISCONNECT = 5;
    public static final int MESSAGE_BLUETOOTH_ERROR = 6;

    // Key names received from the Service Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String MESSAGE = "message";

    public Context context;
    public static boolean disconnectPressed;

    public Bluetooth(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        currentState = STATE_NONE;
    }

    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket bluetoothServerSocket;
        private String socketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tempSocket = null;
            if (secure) {
                socketType = "Secure";
            } else {
                socketType = "Insecure";
            }

            // Create a new listening server socket
            try {
                if (secure) {
                    tempSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                            NAME_SECURE, MY_UUID_SECURE);
                } else {
                    tempSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
            }
            bluetoothServerSocket = tempSocket;
        }

        public void run() {
            setName("AcceptThread" + socketType);

            BluetoothSocket bluetoothSocket;

            // Listen to the server socket if we're not connected
            while (currentState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    bluetoothSocket = bluetoothServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If a connection was accepted
                if (bluetoothSocket != null) {
                    synchronized (Bluetooth.this) {
                        switch (currentState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(bluetoothSocket, bluetoothSocket.getRemoteDevice(),
                                        socketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate
                                // new socket.
                                try {
                                    bluetoothSocket.close();
                                } catch (IOException e) {
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;
        private String socketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            bluetoothDevice = device;
            BluetoothSocket tempSocket = null;
            if (secure) {
                socketType = "Secure";
            } else {
                socketType = "Insecure";
            }

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tempSocket = device
                            .createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                } else {
                    tempSocket = device
                            .createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }
            } catch (IOException e) {
            }
            bluetoothSocket = tempSocket;
        }

        public void run() {
            setName("ConnectThread" + socketType);

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                bluetoothSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    bluetoothSocket.close();
                } catch (IOException e2) {
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (Bluetooth.this) {
                connectThread = null;
            }

            // Start the connected thread
            connected(bluetoothSocket, bluetoothDevice, socketType);
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device. It handles all
     * incoming and outgoing transmissions.
     */

    private class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Send the name of the connected device back to the UI Activity
            Message message = handler.obtainMessage(MESSAGE_READ);
            Bundle bundle = new Bundle();
            bundle.putByteArray(MESSAGE, buffer);
            message.setData(bundle);

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);

                    String received = new String(buffer, "ASCII");
                    Character key = Character.toLowerCase((char) received.charAt(1));

                    // Movement keywords
                    // Deciphers input and sends it to initiate robot movements
                    if ((key == 'b') || (key == 'r') || (key == 'l') || (key == 'f')) {

                        // Displays feedback from robot returns after pressing
                        // keywords
                        handler.obtainMessage(
                                MainActivity.DISPLAY_MOVEMENT_AND_STATUS,
                                bytes, -1, received).sendToTarget();

                        // Reset grid
                        // Redisplay information of grid
                    } else if (key == 'g') {

                        // Updates grid
                        handler.obtainMessage(MainActivity.GRID_UPDATE, bytes,
                                -1, received).sendToTarget();

                        buffer = new byte[1024];

                    } else if (key == 's') {

                        // Displays feedback from robot returns after pressing keywords
                        handler.obtainMessage(MainActivity.STATUS_UPDATE, bytes,
                                -1, received).sendToTarget();

                        buffer = new byte[1024];
                    } else {
                        //Other random info, most likely a result from debug tools testing bluetooth connection.
                        for (byte aBuffer : buffer) {
                            received += (char) aBuffer;
                        }
                        handler.obtainMessage(MainActivity.STATUS_UPDATE,bytes,
                                -1, received).sendToTarget();
                        buffer = new byte[1024];
                    }

                    Log.i("Received data", received);
                } catch (IOException e) {
                    connectionLost(); //To sort reason of disconnection later.
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer
         *            The bytes to write
         */


        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);

                // Share the sent message back to the UI Activity
                handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(MESSAGE_BLUETOOTH_ERROR);
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE, context.getString(R.string.error_connection_failed));
        msg.setData(bundle);
        handler.sendMessage(msg);
        // Start the service over to restart listening mode
        Bluetooth.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity

        if (!disconnectPressed && bluetoothAdapter.isEnabled()) {
			/* Disconnection is caused by anything except manual disconnection from the android device side.
			If BT function is disabled from the android device, it will be also treated as same disconnection.
			However, if disconnection is initiated by the AMD Tool, it will be simulated as lost connection instead.
			This the time when the android app still listen for reconnection attempts from AMD Tool.
			*/
            Message msg = handler.obtainMessage(MESSAGE_BLUETOOTH_DISCONNECT);
            Bundle bundle = new Bundle();
            bundle.putString(MESSAGE, context.getString(R.string.error_connection_lost));
            msg.setData(bundle);
            handler.sendMessage(msg);

            // Start the service over to restart listening mode
            Bluetooth.this.start();
        }
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state
     *            An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        currentState = state;

        // Give the new state to the Handler so the UI Activity can update
        handler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return currentState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (secureAcceptThread == null) {
            secureAcceptThread = new AcceptThread(true);
            secureAcceptThread.start();
        }
        if (insecureAcceptThread == null) {
            insecureAcceptThread = new AcceptThread(false);
            insecureAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device
     *            The BluetoothDevice to connect
     * @param secure
     *            Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        // Cancel any thread attempting to make a connection
        if (currentState == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device, secure);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket
     *            The BluetoothSocket on which the connection was made
     * @param device
     *            The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device, final String socketType) {
        // Cancel the thread that completed the connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one
        // device
        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }
        if (insecureAcceptThread != null) {
            insecureAcceptThread.cancel();
            insecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket, socketType);
        connectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = handler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }

        if (insecureAcceptThread != null) {
            insecureAcceptThread.cancel();
            insecureAcceptThread = null;
        }
        setState(STATE_NONE);

        if (disconnectPressed) {
            Message msg = handler.obtainMessage(MESSAGE_BLUETOOTH_DISCONNECT);
            Bundle bundle = new Bundle();
            bundle.putString(MESSAGE, context.getString(R.string.notification_device_disconnected));
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out
     *            The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread tempThread;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (currentState != STATE_CONNECTED)
                return;
            tempThread = connectedThread;
        }
        // Perform the write unsynchronized
        tempThread.write(out);
    }
}
