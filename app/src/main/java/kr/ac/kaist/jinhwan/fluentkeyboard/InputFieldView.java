package kr.ac.kaist.jinhwan.fluentkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.util.LinkedList;

import static kr.ac.kaist.jinhwan.fluentkeyboard.Direction.getDirection;
import static kr.ac.kaist.jinhwan.fluentkeyboard.Direction.getDirection4;


public class InputFieldView extends ViewGroup {

    private float mCurX, mCurY, mDownX, mDownY, mLastX, mLastY;
    private float LAST_INPUT_RADIUS = 100;
    private float MIN_FLICK_RADIUS = S.getInstance().minFlickRadius;
    private float UI_SIZE = S.getInstance().maxFlickRadius;

    private Paint paint = new Paint();
    private Paint last_paint = new Paint();
    private Paint keypad2Paint = new Paint();

    private boolean flick_valid = false;
    private int keyPadState = 0;
    private int MAX_KEY_PAD =2;
    private int keyPadInterval = 300;
    private long lastAClickTime = 0;

    private long lastDownTime = 0;
    private long longPressInterval = 1000;

    private boolean isMoving, isLastInput =false;

    public RingUIView ringUIView;

    private enum keyPadType{
        J1,J2,M //j =자음, m = 모음
    }



    private class Coord{
        public float x;
        public float y;
        public Coord(double x, double y){
            this.x = (float)x;
            this.y = (float)y;
        }
    }

    LinkedList<Coord> UIPoints = new LinkedList<>();

    MessageListener messageListener;

    public InputFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(6f);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);

        last_paint.set(paint);
        last_paint.setColor(Color.MAGENTA);

        keypad2Paint.set(paint);
        keypad2Paint.setColor(Color.BLUE);
        setWillNotDraw(false);

        // for ui drawing
        LinkedList<Double> radianList = new LinkedList<>();
        double radian = -Math.PI/8;
        for(int i = 0 ; i < 8 ;i ++){
            radianList.add(radian);
            radian += Math.PI/4;
        }
        for(double rad : radianList){
            UIPoints.add(new Coord(UI_SIZE*Math.cos(rad), UI_SIZE*Math.sin(rad)));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public void setMessageListener(MessageListener ml){
        messageListener = ml;
    }
    private void printDirection(String s) {
        messageListener.listenMessage(MessageListener.Type.direction, s);
    }

    private void printText(String s){
        messageListener.listenMessage(MessageListener.Type.text, s);
    }

    private void printText(char s){
       printText(""+s);
    }

    public void setLastInputRadius(float f){
        LAST_INPUT_RADIUS = f;
        Log.d("inputFiledView", String.format("last input radius : %f", LAST_INPUT_RADIUS));
    }
    public void setMinFlickRadius(float f){
        MIN_FLICK_RADIUS = f;
        Log.d("inputFiledView", String.format("min flick radius : %f", MIN_FLICK_RADIUS));
    }


    public void setLastInput(float x, float y){
        Log.d("InputField", String.format("try to set last input to %f,%f", x, y));
        if(S.getInstance().fixLastInput){
            if(mLastX == 0 && mLastY == 0){
                mLastX = x;
                mLastY = y;
                Log.d("InputField", String.format("last input changed  to %f,%f", x, y));
            }
        }else{
            mLastX = x;
            mLastY = y;
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        mCurX=x;
        mCurY=y;

        isMoving = false;

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                isMoving = true;

                if(Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2)) > MIN_FLICK_RADIUS){
                }else{
                    flick_valid = true;
                    if(System.currentTimeMillis() - lastDownTime > longPressInterval){
                        //force last position
                        mLastX = x;
                        mLastY =y;
                    }
                    flick_valid =false;
                }


                //Log.v("inputFiledView", "move");
                invalidate();
                break;

            case MotionEvent.ACTION_DOWN:
                if(System.currentTimeMillis() - lastAClickTime > keyPadInterval){
                    keyPadState = 0;
                }
                mDownX = x;
                mDownY = y;
                flick_valid = false;
                lastDownTime = System.currentTimeMillis();
                //Log.d("inputFiledView", "down");
                break;
            case MotionEvent.ACTION_UP:
                if(isLastInput && Math.sqrt(Math.pow(mLastX-mDownX,2) + Math.pow(mLastY-mDownY,2)) < LAST_INPUT_RADIUS){
                    // starting point == LastInputCircle
                    if(Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2)) > MIN_FLICK_RADIUS){
                        //AFlick
                        //printDirection("<font color='magenta'>Flick</font>");
                        Direction dir = getDirection4(mDownX, mDownY, x, y);
                        printDirection(String.format("<font color='magenta'>%s</font>,", dir.toString()));
                        printText(mapInputToKey(keyPadType.M, dir));
                        isLastInput = true;
                        setLastInput(x,y);
                    }else{
                        //AClick
                        printDirection("<font color='magenta'>Click</font>");
                        printText(mapInputToKey(keyPadType.M, Direction.NON));
                        isLastInput = true;
                        setLastInput(x,y);
                    }
                }else{
                    if(Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2)) > MIN_FLICK_RADIUS){
                        // normal flick end
                        Direction dir = getDirection(mDownX, mDownY, x, y);
                        if(dir != Direction.W) {
                            if (keyPadState == 0) {
                                printText(mapInputToKey(keyPadType.J1, dir));
                                printDirection(dir.toString());
                            } else if (keyPadState == 1) {
                                printText(mapInputToKey(keyPadType.J2, dir));
                                printDirection(String.format("<font color='blue'>%s</font>,", dir.toString()));
                            }
                        }else if(dir == Direction.W){
                            if (keyPadState == 0){
                                Log.d("inputfieldview", "bs");
                                messageListener.listenMessage(MessageListener.Type.special, "bs");
                            }else if (keyPadState == 1) {

                            }
                        }
                        isLastInput = true;
                        setLastInput(x,y);
                    }else{
                        // normal click or press end
                        lastAClickTime = System.currentTimeMillis();
                        printDirection("A_click ");
                        keyPadState = ++keyPadState % MAX_KEY_PAD;

                        isLastInput = true;
                    }
                }
                isMoving =false;
                invalidate();
                break;
        }

        //Log.d("inputFiledView", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));
        ringUIView.otherOnTouchEvent(e);

        return true;
    }







    private char mapInputToKey(keyPadType k, Direction d){
        if(k == keyPadType.J1){
            switch (d){
                case E:
                    return 'ㅂ';
                case NE:
                    return 'ㄷ';
                case N:
                    return 'ㄴ';
                case NW:
                    return 'ㄱ';
                case W:
                    //backsapce
                    return ' ';
                case SW:
                    return 'ㅇ';
                case S:
                    return 'ㅈ';
                case SE:
                    return 'ㅅ';
            }
        }else if(k == keyPadType.J2){
            switch (d){
                case E:
                    return 'ㅍ';
                case NE:
                    return 'ㅌ';
                case N:
                    return 'ㄹ';
                case NW:
                    return 'ㅋ';
                case W:
                    //bs
                    return ' ';
                case SW:
                    return 'ㅁ';
                case S:
                    return 'ㅊ';
                case SE:
                    return 'ㅎ';
            }
        }else if(k == keyPadType.M){
            switch (d){
                case E:
                case W:
                    return 'ㅡ';
                case N:
                case S:
                    return 'ㅣ';
                case NON:
                    return '·';
            }
        }
        return 'E';
    }

    private void drawGuide(Canvas canvas, int xOff, int yOff){
        if(isMoving) {
            canvas.drawLine(mDownX+xOff, mDownY+yOff, mCurX+xOff, mCurY+yOff, flick_valid ? last_paint : paint);
            canvas.drawCircle(mDownX+xOff,mDownY+yOff,MIN_FLICK_RADIUS,flick_valid ? last_paint : paint);
            for(Coord coord :UIPoints){
                canvas.drawLine(mDownX+xOff, mDownY+yOff, mDownX+coord.x+xOff, mDownY+coord.y+yOff, keyPadState ==0 ?paint : keypad2Paint);
            }
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("onDraw", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));

        drawGuide(canvas,0,0);
        drawGuide(canvas, -300,-300);

        if(isLastInput){
            canvas.drawCircle(mLastX, mLastY, LAST_INPUT_RADIUS, last_paint);
        }
    }


}
