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



public class InputFieldView extends ViewGroup {

    private float mCurX, mCurY, mDownX, mDownY, mLastX, mLastY;
    private float LAST_INPUT_RADIUS = 100;
    private float MIN_FLICK_RADIUS = 100;
    private float UI_SIZE = 200;

    private Paint paint = new Paint();
    private Paint last_paint = new Paint();
    private Paint keypad2Paint = new Paint();

    private boolean flick_valid = false;
    private int keyPadState = 0;
    private int MAX_KEY_PAD =2;
    private int keyPadInterval = 300;
    private long lastAClick = 0;

    private boolean isMoving, isLastInput =false;

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


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        mCurX = x;
        mCurY =y;

        isMoving = false;

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                isMoving = true;

                if(Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2)) > MIN_FLICK_RADIUS){
                    flick_valid = true;
                }else{
                    flick_valid =false;
                }
                //Log.v("inputFiledView", "move");
                invalidate();
                break;

            case MotionEvent.ACTION_DOWN:
                if(System.currentTimeMillis() -lastAClick > keyPadInterval){
                    keyPadState = 0;
                }
                mDownX = x;
                mDownY = y;
                flick_valid = false;
                //Log.d("inputFiledView", "down");
                break;
            case MotionEvent.ACTION_UP:
                if(isLastInput && Math.sqrt(Math.pow(mLastX-mDownX,2) + Math.pow(mLastY-mDownY,2)) < LAST_INPUT_RADIUS){
                //is start point in LastInputCircle
                    if(Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2)) > MIN_FLICK_RADIUS){
                        //printDirection("<font color='magenta'>Flick</font>");
                        Direction dir = getDirection4(mDownX, mDownY, x, y);
                        printDirection(String.format("<font color='magenta'>%s</font>,", dir.toString()));
                        printText(mapInputToKey(keyPadType.M, dir));
                        isLastInput = true;
                        mLastX =x;
                        mLastY = y;

                    }else{
                        printDirection("<font color='magenta'>Click</font>");
                        printText(mapInputToKey(keyPadType.M, Direction.NON));
                        isLastInput = true;
                        mLastX =x;
                        mLastY = y;
                    }
                }else{
                    if(Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2)) > MIN_FLICK_RADIUS){
                        Direction dir = getDirection(mDownX, mDownY, x, y);
                        if(dir != Direction.SE) {
                            if (keyPadState == 0) {
                                printText(mapInputToKey(keyPadType.J1, dir));
                                printDirection(dir.toString());
                            } else if (keyPadState == 1) {
                                printText(mapInputToKey(keyPadType.J2, dir));
                                printDirection(String.format("<font color='blue'>%s</font>,", dir.toString()));
                            }
                        }else if(dir == Direction.SE){
                            if (keyPadState == 0){
                                Log.d("inputfieldview", "bs");
                                messageListener.listenMessage(MessageListener.Type.special, "bs");
                            }else if (keyPadState == 1) {

                            }
                        }
                        isLastInput = true;
                        mLastX =x;
                        mLastY = y;
                    }else{
                        printDirection("A_click ");
                        keyPadState = ++keyPadState%MAX_KEY_PAD;
                        lastAClick = System.currentTimeMillis();
                        isLastInput = true;
                    }
                }

                isMoving =false;
                invalidate();
                break;
        }

        //Log.d("inputFiledView", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));


        return true;
    }


    /**
     * Given two points in the plane p1=(x1, x2) and p2=(y1, y1), this method
     * returns the direction that an arrow pointing from p1 to p2 would have.
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the direction
     */
    public Direction getDirection(float x1, float y1, float x2, float y2){
        double angle = getAngle(x1, y1, x2, y2);
        return Direction.get(angle);
    }

    public Direction getDirection4(float x1, float y1, float x2, float y2){
        double angle = getAngle(x1, y1, x2, y2);
        return Direction.get4(angle);
    }

    /**
     *
     * Finds the angle between two points in the plane (x1,y1) and (x2, y2)
     * The angle is measured with 0/360 being the X-axis to the right, angles
     * increase counter clockwise.
     *
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the angle between two points
     */
    public double getAngle(float x1, float y1, float x2, float y2) {
        double rad = Math.atan2(y1-y2,x2-x1) + Math.PI;
        return (rad*180/Math.PI + 180)%360;
    }


    public enum Direction {
        E,
        NE,
        N,
        NW,
        W,
        SW,
        S,
        SE,
        NON;

        /**
         * Returns a direction given an angle.
         * Directions are defined as follows:
         * <p/>
         * Up: [45, 135]
         * Right: [0,45] and [315, 360]
         * Down: [225, 315]
         * Left: [135, 225]
         *
         * @param angle an angle from 0 to 360 - e
         * @return the direction of an angle
         */
        public static Direction get(double angle) {
            if (inRange(angle, 0, 360*1/16) || inRange(angle, 360*15/16, 360)) {
                return Direction.E;
            }else if (inRange(angle, 360*1/16, 360*3/16) ) {
                return Direction.NE;
            }else if (inRange(angle, 360*3/16, 360*5/16) ) {
                return Direction.N;
            }else if (inRange(angle, 360*5/16, 360*7/16) ) {
                return Direction.NW;
            }else if (inRange(angle, 360*7/16, 360*9/16) ) {
                return Direction.W;
            }else if (inRange(angle, 360*9/16, 360*11/16) ) {
                return Direction.SW;
            }else if (inRange(angle, 360*11/16, 360*13/16) ) {
                return Direction.S;
            }else if (inRange(angle, 360*13/16, 360*15/16) ) {
                return Direction.SE;
            }else{
                return null;
            }
/*
            if (inRange(angle, 45, 135)) {
                return Direction.N;
            } else if (inRange(angle, 0, 360*1/16) || inRange(angle, 360*15/16, 360)) {
                return Direction.E;
            } else if (inRange(angle, 225, 315)) {
                return Direction.S;
            } else {
                return Direction.W;
            }
*/
        }
        public static Direction get4(double angle) {
            if (inRange(angle, 45, 135)) {
                return Direction.N;
            } else if (inRange(angle, 0, 360*1/16) || inRange(angle, 360*15/16, 360)) {
                return Direction.E;
            } else if (inRange(angle, 225, 315)) {
                return Direction.S;
            } else {
                return Direction.W;
            }
        }


        /**
         * @param angle an angle
         * @param init  the initial bound
         * @param end   the final bound
         * @return returns true if the given angle is in the interval [init, end).
         */
        private static boolean inRange(double angle, float init, float end) {
            return (angle >= init) && (angle < end);
        }
    }

    private char mapInputToKey(keyPadType k, Direction d){
        if(k == keyPadType.J1){
            switch (d){
                case E:
                    return 'ㅇ';
                case NE:
                    return 'ㅈ';
                case N:
                    return 'ㅅ';
                case NW:
                    return 'ㅂ';
                case W:
                    return 'ㄷ';
                case SW:
                    return 'ㄴ';
                case S:
                    return 'ㄱ';
            }
        }else if(k == keyPadType.J2){
            switch (d){
                case E:
                    return 'ㅁ';
                case NE:
                    return 'ㅊ';
                case N:
                    return 'ㅎ';
                case NW:
                    return 'ㅍ';
                case W:
                    return 'ㅌ';
                case SW:
                    return 'ㄹ';
                case S:
                    return 'ㅋ';
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


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("onDraw", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));
        if(isMoving) {
            canvas.drawLine(mDownX, mDownY, mCurX, mCurY, flick_valid ? last_paint : paint);
            canvas.drawCircle(mDownX,mDownY,MIN_FLICK_RADIUS,flick_valid ? last_paint : paint);
            for(Coord coord :UIPoints){
                canvas.drawLine(mDownX, mDownY, mDownX+coord.x, mDownY+coord.y, keyPadState ==0 ?paint : keypad2Paint);
            }
        }
        if(isLastInput){
            canvas.drawCircle(mLastX, mLastY, LAST_INPUT_RADIUS, last_paint);
        }
    }
}
