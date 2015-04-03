package kr.ac.kaist.jinhwan.fluentkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;


public class InputFieldView extends ViewGroup {

    private float mPreviousX, mPreviousY,mCurX, mCurY, mDownX, mDownY, mLastX, mLastY;
    private final float LastINPUTRADIUS = 100;
    private Paint paint = new Paint();

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private boolean isMoving, isLastInpt =false;

    public InputFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(6f);
        paint.setStrokeJoin(Paint.Join.ROUND);

        setWillNotDraw(false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        mPreviousX = mCurX;
        mPreviousY = mCurY;
        mCurX = x;
        mCurY =y;

        isMoving = false;

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                isMoving = true;
                Log.v("inputFiledView", "move");
                invalidate();
                break;

            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                Log.d("inputFiledView", "down");
            case MotionEvent.ACTION_UP:

                mLastX =x;
                mLastY = y;
                isMoving =false;
                isLastInpt = true;
                break;
        }

        //Log.d("inputFiledView", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));


        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("onDraw", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));
        if(isMoving) {
            canvas.drawLine(mDownX, mDownY, mCurX, mCurY, paint);
        }
        if(isLastInpt){

        }
    }
}
