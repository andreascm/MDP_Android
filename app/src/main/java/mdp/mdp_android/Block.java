package mdp.mdp_android;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by andreaschrisnamayong on 9/26/16.
 */
public class Block {
    private final IntBuffer mVertexBuffer;

    public Block() {
        int one = 65536;
        int half = one/2;
        int vertices[] = {
                // FRONT
                -half, -half, half, half, -half, half,
                -half, half, half, half, half, half,
                // BACK
                -half, -half, -half, -half, half, -half,
                half, -half, -half, half, half, -half,
                // LEFT
                -half, -half, half, -half, half, half,
                -half, -half, -half, -half, half, -half,
                // RIGHT
                half, -half, -half, half, half, -half,
                half, -half, half, half, half, half,
                // TOP
                -half, half, half, half, half, half,
                -half, half, -half, half, half, -half,
                // BOTTOM
                -half, -half, half, -half, -half, -half,
                half, -half, half, half, -half, -half,
        };

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asIntBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
    }

    public void draw(GL10 gl) {
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);

        gl.glColor4f(1, 1, 1, 1);
        gl.glNormal3f(-1, 0, 0);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4);
        gl.glNormal3f(1, 0, 0);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4);

        gl.glColor4f(1, 1, 1, 1);
        gl.glNormal3f(0, 1, 0);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4);
        gl.glNormal3f(0, -1, 0);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4);
    }
}
