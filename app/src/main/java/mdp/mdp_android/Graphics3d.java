package mdp.mdp_android;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class Graphics3d extends ActionBarActivity {
    GLView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glView = new GLView(this);
        setContentView(glView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();
    }
}
