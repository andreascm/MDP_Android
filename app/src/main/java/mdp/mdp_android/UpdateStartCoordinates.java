package mdp.mdp_android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UpdateStartCoordinates extends ActionBarActivity {

    private SharedPreferences mSharedPreferences;
    private EditText edittextxpos, edittextypos;
    private Button saveButton;
    private Bluetooth bluetooth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_start_coordinates);
        bluetooth = MainActivity.getBluetooth();
        edittextxpos = (EditText) findViewById(R.id.edittextxpos);
        edittextypos = (EditText) findViewById(R.id.edittextypos);

        saveButton = (Button) findViewById(R.id.coordinateSavebutton);

        mSharedPreferences = getSharedPreferences("UserConfiguration",
                MODE_PRIVATE);

        loadConfig();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int xpos = Integer.parseInt(edittextxpos.getText().toString());
                int ypos = Integer.parseInt(edittextypos.getText().toString());

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt("xpos", xpos);
                editor.putInt("ypos", ypos);
                editor.apply();
                if (bluetooth != null) {
                    bluetooth.write(("px" + String.valueOf(xpos)).getBytes());
                    bluetooth.write(("py" + String.valueOf(ypos)).getBytes());
                }
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    private void loadConfig() {

        edittextxpos.setText(String.valueOf(mSharedPreferences.getInt("xpos", 1)));
        edittextypos.setText(String.valueOf(mSharedPreferences.getInt("ypos", 1)));

    }
}