package mdp.mdp_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by andreaschrisnamayong on 9/16/16.
 */
public class MapView extends View {
    private int numColumn;
    private int numRow;
    private int cellWidth;
    private int cellHeight;
    private Paint paintDiscovered = new Paint();
    private Paint paintObstacle = new Paint();
    private int[][] painted;

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attributes) {
        super(context, attributes);
        paintDiscovered.setStyle(Paint.Style.FILL_AND_STROKE);
        paintObstacle.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void recalculateDimension() {
        if (numColumn < 1 || numRow < 1) {
            return;
        }

        cellWidth = getWidth() / numColumn;
        cellHeight = getHeight() / numRow;

        painted = new int[numColumn][numRow];
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        recalculateDimension();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        if (numColumn < 1 || numRow < 1) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        for (int i=0; i<numRow; i++) {
            for(int j=0; j<numColumn; j++) {
                if (painted[i][j] == 1) {
                    canvas.drawRect(i*cellHeight, j*cellWidth, (i+1)*cellHeight, (j+1)*cellWidth, paintDiscovered);
                } else if (painted[i][j] == -1) {
                    canvas.drawRect(i*cellHeight, j*cellWidth, (i+1)*cellHeight, (j+1)*cellWidth, paintDiscovered);
                }
            }
        }
    }

    public void setNumColumn(int column) {
        numColumn = column;
        recalculateDimension();
    }

    public void setNumRow(int row) {
        numRow = row;
        recalculateDimension();
    }

    public void updatePainted(int[][] map) {
        painted = map;
        invalidate();
    }

    public int getNumColumn() {
        return numColumn;
    }

    public int getNumRow() {
        return numRow;
    }
}
