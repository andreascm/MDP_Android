package mdp.mdp_android;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MDFString extends ActionBarActivity {
    private TextView mExplored;
    private TextView mObstacles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdfstring);

        mExplored = (TextView) findViewById(R.id.explored);
        mObstacles = (TextView) findViewById(R.id.obstacles);

        mExplored.setText(MainActivity.explored);
        mObstacles.setText(MainActivity.obstacles);
    }
}
