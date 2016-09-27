package mdp.mdp_android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class BluetoothTesting extends ActionBarActivity {

    private SharedPreferences mSharedPreferences;
    private EditText sendmessage, receivedmessage;
    private Button sendmessagebutton;
    private Bluetooth bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_testing);

        receivedmessage= (EditText) findViewById(R.id.receivedmessage) ;
        sendmessage = (EditText) findViewById(R.id.sendmessage);
        bluetooth = MainActivity.getBluetooth();

        sendmessagebutton = (Button) findViewById(R.id.sendmessagebutton);
        mSharedPreferences = getSharedPreferences("UserConfiguration",
                MODE_PRIVATE);

        receivedmessage.setText(mSharedPreferences.getString("receivedmessage",""));
        sendmessage.setText(mSharedPreferences.getString("sendmessage", ""));

        sendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = sendmessage.getText().toString();

                if (bluetooth != null) {
                    bluetooth.write(message.getBytes());
                }
                finish();
            }
        });
    }
}