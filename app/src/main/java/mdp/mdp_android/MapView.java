package mdp.mdp_android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
    private int current_x;
    private int current_y;
    private int direction;

    public MapView(Context context) {
        this(context, null, 0, 0, 0);
    }

    public MapView(Context context, int[][] map, int x, int y, int dir) {
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
        current_x = x;
        current_y = y;
        direction = dir;
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
                if (painted[i][j] == 1) {
                    canvas.drawRect(i*cellWidth, (numRow-j-1)*cellHeight, (i+1)*cellWidth, (numRow-j)*cellHeight, paintDiscovered);
                    canvas.drawRect(i*cellWidth, (numRow-j-1)*cellHeight, (i+1)*cellWidth, (numRow-j)*cellHeight, paintGrid);
                } else if (painted[i][j] == -1) {
                    canvas.drawRect(i*cellWidth, (numRow-j-1)*cellHeight, (i+1)*cellWidth, (numRow-j)*cellHeight, paintObstacle);
                    canvas.drawRect(i*cellWidth, (numRow-j-1)*cellHeight, (i+1)*cellWidth, (numRow-j)*cellHeight, paintGrid);
                }
                canvas.drawRect(i*cellWidth, j*cellHeight, (i+1)*cellWidth, (j+1)*cellHeight, paintGrid);
            }
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.robot);
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.3), (int) (bitmap.getHeight() * 0.3), true);

        Matrix matrix = new Matrix();
        if (direction == 2) {
            matrix.postRotate(180);
        } else if (direction == 3) {
            matrix.postRotate(-90);
        } else if (direction == 4) {
            matrix.postRotate(90);
        }

        Bitmap rotated = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), matrix, true);

        canvas.drawBitmap(rotated, (float) ((current_x-0.55)*cellWidth), (float) ((numRow-current_y-1.85)*cellHeight), null);
    }

    public void setNumColumn(int column) {
        numColumn = column;
        recalculateDimension();
    }

    public void setNumRow(int row) {
        numRow = row;
        recalculateDimension();
    }

    public void setCurrentX(int x) {
        current_x = x;
    }

    public void setCurrentY(int y) {
        current_y = y;
    }

    public void setDirection(int dir) {
        direction = dir;
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
