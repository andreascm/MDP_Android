package mdp.mdp_android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class BluetoothTesting extends ActionBarActivity {

    private SharedPreferences mSharedPreferences;
    private EditText sendmessage, receivedmessage;
    private Button sendmessagebutton;
    private Bluetooth bluetooth;

    public static final int RECEIVE_MESSAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_testing);

        receivedmessage = (EditText) findViewById(R.id.receivedmessage);
        sendmessage = (EditText) findViewById(R.id.sendmessage);
        bluetooth = MainActivity.getBluetooth();

        sendmessagebutton = (Button) findViewById(R.id.sendmessagebutton);
        mSharedPreferences = getSharedPreferences("UserConfiguration",
                MODE_PRIVATE);

        sendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = sendmessage.getText().toString();

                if (bluetooth != null) {
                    bluetooth.write(message.getBytes());
                }

            }
        });
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case RECEIVE_MESSAGE:
                    String receivemessage = (String) msg.obj;
                    receivedmessage.setText(receivemessage);
                    receivedmessage.postInvalidate();
                    break;

            }
        }
    };
}

