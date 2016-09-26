package mdp.mdp_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
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
    private Paint paintGrid = new Paint();
    private int[][] painted = new int[15][20];

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, int[][] map) {
        super(context);
        paintDiscovered.setColor(Color.YELLOW);
        paintDiscovered.setStyle(Paint.Style.FILL);
        paintObstacle.setColor(Color.BLACK);
        paintObstacle.setStyle(Paint.Style.FILL);
        paintGrid.setColor(Color.DKGRAY);
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(3);
        cellWidth = 10;
        cellHeight = 10;
        numColumn = 15;
        numRow = 20;
        painted = map;
    }

    public void recalculateDimension() {
        if (numColumn < 1 || numRow < 1) {
            return;
        }

        cellWidth = getWidth() / numColumn;
        cellHeight = getHeight() / numRow;

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

        for (int i=0; i<numColumn; i++) {
            for(int j=0; j<numRow; j++) {
                canvas.drawRect(i*cellWidth, j*cellHeight, (i+1)*cellWidth, (j+1)*cellHeight, paintGrid);
                if (painted[i][j] == 1) {
                    canvas.drawRect(i*cellWidth, (numRow-j-1)*cellHeight, (i+1)*cellWidth, (numRow-j)*cellHeight, paintDiscovered);
                } else if (painted[i][j] == -1) {
                    canvas.drawRect(i*cellWidth, (numRow-j-1)*cellHeight, (i+1)*cellWidth, (numRow-j)*cellHeight, paintObstacle);
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
