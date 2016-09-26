package mdp.mdp_android;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by andreaschrisnamayong on 9/26/16.
 */
public class GLView extends GLSurfaceView {
    private final GLRenderer glRenderer;

    public GLView(Context context) {
        super(context);
        glRenderer = new GLRenderer(context);
        setRenderer(glRenderer);
    }
}
