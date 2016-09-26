package mdp.mdp_android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.EditText;

public class ReconfigureButtons extends ActionBarActivity {

    private SharedPreferences mSharedPreferences;
    private EditText edittextF1Config, edittextF2Config;
    private String F1Config, F2Config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconfigure_buttons);
    }

}
