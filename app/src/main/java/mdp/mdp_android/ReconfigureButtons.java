package mdp.mdp_android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ReconfigureButtons extends ActionBarActivity {

    private SharedPreferences mSharedPreferences;
    private EditText edittextF1Config, edittextF2Config;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconfigure_buttons);

        edittextF1Config = (EditText) findViewById(R.id.edittextF1Config);
        edittextF2Config = (EditText) findViewById(R.id.edittextF2Config);

        saveButton = (Button) findViewById(R.id.reconfigureSavebutton);

        mSharedPreferences = getSharedPreferences("UserConfiguration",
                MODE_PRIVATE);

        loadConfig();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String f1 = edittextF1Config.getText().toString();
                String f2 = edittextF2Config.getText().toString();

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("f1", f1);
                editor.putString("f2", f2);
                editor.apply();
                finish();
            }
        });
    }

    private void loadConfig() {

        edittextF1Config.setText(mSharedPreferences.getString("f1", ""));
        edittextF2Config.setText(mSharedPreferences.getString("f2", ""));

    }
}