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
    private int paintMode;
    private Paint paintDiscovered = new Paint();
    private Paint paintObstacle = new Paint();
    private Paint paintGrid = new Paint();
    private int[][] painted = new int[15][20];

    public MapView(Context context) {
        this(context, 0);
    }

    public MapView(Context context, int mode) {
        super(context);
        paintMode = mode;
        paintDiscovered.setColor(Color.YELLOW);
        paintDiscovered.setStyle(Paint.Style.FILL);
        paintObstacle.setColor(Color.BLACK);
        paintObstacle.setStyle(Paint.Style.FILL);
        paintGrid.setColor(Color.GRAY);
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(2);
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

        if (paintMode == 0) {
            painted = new int[15][20];
        }

        int width = getWidth();
        int height = getHeight();

        for (int i=0; i<numRow; i++) {
            for(int j=0; j<numColumn; j++) {
                canvas.drawRect(i*cellHeight, j*cellWidth, (i+1)*cellHeight, (j+1)*cellWidth, paintGrid);
                if (painted[i][j] == 1) {
                    canvas.drawRect(i*cellHeight, j*cellWidth, (i+1)*cellHeight, (j+1)*cellWidth, paintDiscovered);
                } else if (painted[i][j] == -1) {
                    canvas.drawRect(i*cellHeight, j*cellWidth, (i+1)*cellHeight, (j+1)*cellWidth, paintObstacle);
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
