package mdp.mdp_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by andreaschrisnamayong on 9/27/16.
 */
public class Joystick extends View implements Runnable {
    private final double RAD = 57.2957795;
    public final static long LOOP_INTERVAL = 100;
    public final static int UP = 3;
    public final static int UP_RIGHT = 2;
    public final static int RIGHT = 1;
    public final static int RIGHT_DOWN = 8;
    public final static int DOWN = 7;
    public final static int DOWN_LEFT = 6;
    public final static int LEFT = 5;
    public final static int LEFT_UP = 4;

    private Thread thread = new Thread(this);
    private OnJoystickMoveListener onJoystickMoveListener;

    private long loopInterval = LOOP_INTERVAL;
    private int xPos = 0;
    private int yPos = 0;
    private int buttonRad;
    private int joystickRad;
    private int lastAngle = 0;
    private int lastPower = 0;
    private double xCenter = 0;
    private double yCenter = 0;

    private Paint joystickPad;
    private Paint joystickPad2;
    private Paint joystickButton;

    public Joystick(Context context) {
        super(context);
        initialize();
    }

    public Joystick(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initialize();
    }

    public Joystick(Context context, AttributeSet attributeSet, int style) {
        super(context, attributeSet, style);
        initialize();
    }

    public void initialize() {
        joystickPad = new Paint(Paint.ANTI_ALIAS_FLAG);
        joystickPad.setColor(Color.GRAY);
        joystickPad.setStyle(Paint.Style.FILL_AND_STROKE);

        joystickPad2 = new Paint();
        joystickPad2.setColor(Color.WHITE);
        joystickPad2.setStyle(Paint.Style.STROKE);

        joystickButton = new Paint(Paint.ANTI_ALIAS_FLAG);
        joystickButton.setColor(Color.DKGRAY);
        joystickButton.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        xPos = (int) getWidth() / 2;
        yPos = (int) getHeight() / 2;
        int d = Math.min(w, h);
        buttonRad = (int) (d / 2 * 0.25);
        joystickRad = (int) (d / 2 * 0.75);
    }

    public int measure(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            return 200;
        } else {
            return specSize;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));
        setMeasuredDimension(d, d);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        xCenter = getWidth() / 2;
        yCenter = getHeight() / 2;

        canvas.drawCircle((int) xCenter, (int) yCenter, joystickRad, joystickPad);
        canvas.drawCircle((int) xCenter, (int) yCenter, joystickRad / 2, joystickPad2);
        canvas.drawCircle((int) xPos, (int) yPos, buttonRad, joystickButton);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xPos = (int) event.getX();
        yPos = (int) event.getY();
        double abs = Math.sqrt((xPos - xCenter) * (xPos - xCenter) + (yPos - yCenter) * (yPos - yCenter));

        if (abs > joystickRad) {
            xPos = (int) ((xPos - xCenter) * joystickRad / abs + xCenter);
            yPos = (int) ((yPos - yCenter) * joystickRad / abs + yCenter);
        }

        invalidate();

        if (event.getAction() == MotionEvent.ACTION_UP) {
            xPos = (int) xCenter;
            yPos = (int) yCenter;
            thread.interrupt();

            if (onJoystickMoveListener != null) {
                onJoystickMoveListener.onValueChanged(getAngle(), getPower(), getDirection());
            }
        }

        if (onJoystickMoveListener != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }

            thread = new Thread(this);
            thread.start();
            onJoystickMoveListener.onValueChanged(getAngle(), getPower(), getDirection());
        }

        return true;
    }

    private int getAngle() {
        if (xPos > xCenter) {
            if (yPos < yCenter) {
                return lastAngle = (int) (Math.atan((yPos - yCenter)
                        / (xPos - xCenter))
                        * RAD + 90);
            } else if (yPos > yCenter) {
                return lastAngle = (int) (Math.atan((yPos - yCenter)
                        / (xPos - xCenter)) * RAD) + 90;
            } else {
                return lastAngle = 90;
            }
        } else if (xPos < xCenter) {
            if (yPos < yCenter) {
                return lastAngle = (int) (Math.atan((yPos - yCenter)
                        / (xPos - xCenter))
                        * RAD - 90);
            } else if (yPos > yCenter) {
                return lastAngle = (int) (Math.atan((yPos - yCenter)
                        / (xPos - xCenter)) * RAD) - 90;
            } else {
                return lastAngle = -90;
            }
        } else {
            if (yPos <= yCenter) {
                return lastAngle = 0;
            } else {
                if (lastAngle < 0) {
                    return lastAngle = -180;
                } else {
                    return lastAngle = 180;
                }
            }
        }
    }

    private int getPower() {
        return (int) (100 * Math.sqrt((xPos - xCenter)
                * (xPos - xCenter) + (yPos - yCenter)
                * (yPos - yCenter)) / joystickRad);
    }

    private int getDirection() {
        if (lastPower == 0 && lastAngle == 0) {
            return 0;
        }
        int a = 0;
        if (lastAngle <= 0) {
            a = (lastAngle * -1) + 90;
        } else if (lastAngle > 0) {
            if (lastAngle <= 90) {
                a = 90 - lastAngle;
            } else {
                a = 360 - (lastAngle - 90);
            }
        }

        int direction = (int) (((a + 22) / 45) + 1);

        if (direction > 8) {
            direction = 1;
        }
        return direction;
    }

    public void setOnJoystickMoveListener(OnJoystickMoveListener onJoystickMoveListener, long repeatInterval) {
        this.onJoystickMoveListener = onJoystickMoveListener;
        loopInterval = repeatInterval;
    }

    public interface OnJoystickMoveListener {
        public void onValueChanged(int angle, int power, int direction);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (onJoystickMoveListener != null) {
                        onJoystickMoveListener.onValueChanged(getAngle(), getPower(), getDirection());
                    }
                }
            });
            try {
                Thread.sleep(loopInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}