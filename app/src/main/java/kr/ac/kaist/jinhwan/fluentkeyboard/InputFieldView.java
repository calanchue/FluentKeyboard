package kr.ac.kaist.jinhwan.fluentkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static kr.ac.kaist.jinhwan.fluentkeyboard.Direction.getDirection;
import static kr.ac.kaist.jinhwan.fluentkeyboard.Direction.getDirection4;


public class InputFieldView extends ViewGroup {

    private float mCurX, mCurY, mDownX, mDownY, mLastX, mLastY;

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

    private boolean isMoving, isLastInput =false;

    public RingUIView ringUIView;

    private boolean showGestureHistory =false;

    private enum keyPadType{
        J1,J2,M //j =자음, m = 모음
    }

    float hoverX = 0 ;
    float hoverY = 0;



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

        this.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if(S.getInstance().hoverTrack){
                    Log.d("hoverInput", String.format("%f %f", event.getX(), event.getY()));
                    hoverX = event.getX();
                    hoverY = event.getY();
                    invalidate();
                    ringUIView.OnHoverOnVI(event, mLastX, mLastY);
                    return true;
                }
                return false;
            }
        });
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
       printText("" + s);
    }

    private static class InputData{
        float fromX;
        float fromY;
        float toX;
        float toY;
        GestureType type;

        public static enum GestureType{
            AClick,AFlick,Click,Flick,Wild
        }

        long time;
        public InputData(float fromX, float fromY, float toX, float toY,GestureType type){
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX= toX;
            this.toY = toY;
            this.type = type;
            time = System.currentTimeMillis();
        }

        //add wild
        public InputData(float x, float y){
            this.fromX = x;
            this.fromY = y;
            this.toX = x;
            this.toY = y;
            this.type = GestureType.Wild;
            time = System.currentTimeMillis();
        }
    }
    List<InputData> gestureHistory = new LinkedList<>();
    List<InputData> recentInputHistory = new LinkedList<>();
    List<Float[]> lastInputHistory = new ArrayList<>();


    public void setLastInput(float x, float y ,
                             boolean isInputFromA, boolean isFlick){
        Log.d("InputField", String.format("try to set last input to %f,%f", x, y));

        float totalX = 0;
        float totalY = 0;
        switch (S.getInstance().inputOption){
            case Fixed:
                if(mLastX == 0 && mLastY == 0){

                    mLastX = x;
                    mLastY = y;
                    //Log.d("InputField", String.format("last input changed  to %f,%f", x, y));
                }
                break;
            case Last:
                mLastX = x;
                mLastY = y;
                break;
            case AdaptAll:
                //only gather data from Input converges to center

                recentInputHistory.add(gestureHistory.get(gestureHistory.size() - 1));

                while(true){
                    if(recentInputHistory.size() > S.getInstance().adaptHistorySize){
                        recentInputHistory.remove(0);
                    }else{
                        break;
                    }
                }

                totalX  =0;
                totalY = 0;
                for( InputData inputData : recentInputHistory){
                    if(inputData.type == InputData.GestureType.AFlick ){
                        totalX += inputData.fromX;
                        totalY += inputData.fromY;
                    }else {
                        totalX += inputData.toX;
                        totalY += inputData.toY;
                    }
                }
                mLastX = totalX/ recentInputHistory.size();
                mLastY = totalY/ recentInputHistory.size();
                break;
            case AdaptConsonant:
                //only gather data from Input converges to center
                if(isInputFromA){
                    break;
                }

                recentInputHistory.add(gestureHistory.get(gestureHistory.size() - 1));

                while(true){
                    if(recentInputHistory.size() > S.getInstance().adaptHistorySize){
                        recentInputHistory.remove(0);
                    }else{
                        break;
                    }
                }
                totalX  =0;
                totalY = 0;
                for( InputData inputData : recentInputHistory){
                    if(inputData.type == InputData.GestureType.AFlick ){
                        totalX += inputData.fromX;
                        totalY += inputData.fromY;
                    }else {
                        totalX += inputData.toX;
                        totalY += inputData.toY;
                    }
                }
                mLastX = totalX/ recentInputHistory.size();
                mLastY = totalY/ recentInputHistory.size();
                break;
            case AdaptVowel:
                //only gather data from Input converges to center
                if(recentInputHistory.isEmpty()){
                    InputData recentInputData = gestureHistory.get(gestureHistory.size() - 1);
                    recentInputHistory.add(recentInputData);
                    //must me consonant
                    mLastX = recentInputData.toX;
                    mLastY = recentInputData.toY;
                }
                if(!isInputFromA && !recentInputHistory.isEmpty()){
                    break;
                }

                recentInputHistory.add(gestureHistory.get(gestureHistory.size() - 1));

                while(true){
                    if(recentInputHistory.size() > S.getInstance().adaptHistorySize){
                        recentInputHistory.remove(0);
                    }else{
                        break;
                    }
                }

                totalX  =0;
                totalY = 0;
                for( InputData inputData : recentInputHistory){
                    if(inputData.type == InputData.GestureType.AFlick ){
                        totalX += inputData.fromX;
                        totalY += inputData.fromY;
                    }else {
                        totalX += inputData.toX;
                        totalY += inputData.toY;
                    }
                }
                mLastX = totalX/ recentInputHistory.size();
                mLastY = totalY/ recentInputHistory.size();
                break;
        }
/*        if(S.getInstance().inputOption == S.InputOption.Fixed){
            if(mLastX == 0 && mLastY == 0){
                mLastX = x;
                mLastY = y;
                Log.d("InputField", String.format("last input changed  to %f,%f", x, y));
            }
        }else if(S.getInstance().inputOption.Last{
            mLastX = x;
            mLastY = y;
        }*/
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

                if(Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2)) > S.getInstance().minFlickRadius){
                }else{
                    flick_valid = true;
                    if(System.currentTimeMillis() - lastDownTime > S.getInstance().longPressInterval){
                        //force last position
                        recentInputHistory = new ArrayList<>();
                        for(int  i = 0 ; i< S.getInstance().adaptHistorySize; i++){
                            recentInputHistory.add(new InputData(x,y));
                        }
                        mLastX = x;
                        mLastY =y;
                    }
                    flick_valid =false;
                }

                //Log.v("inputFiledView", "move");
                invalidate();
                break;

            case MotionEvent.ACTION_DOWN:
                if(System.currentTimeMillis() - lastAClickTime < keyPadInterval){
                    keyPadState = 1;
                    ringUIView.changeSet(RingUIView.KeyMode.V2);
                }
                mDownX = x;
                mDownY = y;
                flick_valid = false;
                lastDownTime = System.currentTimeMillis();
                //Log.d("inputFiledView", "down");
                break;
            case MotionEvent.ACTION_UP:
                if(isLastInput && Math.sqrt(Math.pow(mLastX-mDownX,2) + Math.pow(mLastY-mDownY,2)) < S.getInstance().lastInputRadius){
                    // starting point == LastInputCircle
                    if(Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2)) > S.getInstance().minFlickRadius){
                        //AFlick
                        //printDirection("<font color='magenta'>Flick</font>");
                        Direction dir = getDirection4(mDownX, mDownY, x, y);
                        printDirection(String.format("<font color='magenta'>%s</font>,", dir.toString()));
                        printText(mapInputToKey(keyPadType.M, dir));
                        isLastInput = true;

                        gestureHistory.add(new InputData(mDownX, mDownY, x, y, InputData.GestureType.AFlick));
                        setLastInput(x,y,true,true);
                    }else{
                        //AClick
                        printDirection("<font color='magenta'>Click</font>");
                        printText(mapInputToKey(keyPadType.M, Direction.NON));
                        isLastInput = true;

                        gestureHistory.add(new InputData(mDownX, mDownY, x, y, InputData.GestureType.AClick));
                        setLastInput(x,y,true,false);
                    }
                }else{
                    if(Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2)) > S.getInstance().minFlickRadius){
                        // normal flick end
                        Direction dir = getDirection(mDownX, mDownY, x, y);

                        // Direction it started
                        Direction startDir  = getDirection4(mLastX, mLastY, mDownX, mDownY);
                        switch (startDir){
                            case N:
                                break;
                            case S:
                                break;
                            case E:
                                break;
                            case W:
                                break;
                        }

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

                        gestureHistory.add(new InputData(mDownX, mDownY, x, y, InputData.GestureType.Flick));
                        setLastInput(x,y,false,true);
                    }else{
                        // normal click or press end
                        lastAClickTime = System.currentTimeMillis();
                        printDirection("A_click ");
                        //keyPadState = ++keyPadState % MAX_KEY_PAD;

                        isLastInput = true;
                    }
                }
                isMoving =false;
                lastInputHistory.add(new Float[]{mLastX, mLastY});

                ringUIView.changeSet(RingUIView.KeyMode.V1);
                keyPadState = 0;

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
            canvas.drawCircle(mDownX+xOff,mDownY+yOff,S.getInstance().minFlickRadius,flick_valid ? last_paint : paint);
            for(Coord coord :UIPoints){
                canvas.drawLine(mDownX+xOff, mDownY+yOff, mDownX+coord.x+xOff, mDownY+coord.y+yOff, keyPadState ==0 ?paint : keypad2Paint);
            }
        }
    }



    public void clearInputHistory(){
        gestureHistory = new ArrayList<>();
    }

    private void drawTimeLastInputChangeGraph(Canvas canvas){
        int startX = getWidth()/2;;
        int startY = getWidth()/2;
        int height = 100;
        int width = 200;



        boolean isFirst =true;
        LinkedList<Double> distanceList = new LinkedList<>();
        Float[] prev = {0f,0f};
        Double distanceMax =0.0;
        for(Float[] lastInput : lastInputHistory){
            if(isFirst){
                prev = lastInput;
                continue;
            }
            double distance = Math.sqrt(Math.pow(lastInput[0] - prev[0], 2) + Math.pow(lastInput[1] - prev[1], 2));
            distanceList.add(distance);
            if(distance > distanceMax){
                distanceMax = distance;
            }
        }

        Double delta = (double)width/(double)distanceList.size();
        Double currPos = (double)startX;
        for(Double distance : distanceList){
            //canvas.drawLine();
            currPos+=delta;
        }


    }

    public void showVIPosHistorey(){
        drawVIPosHistory = true;
        invalidate();
    }

    //vowel input history
    public boolean drawVIPosHistory = false;
    private void drawVIPosHistory(Canvas canvas){
        boolean first = true;
        Paint linePaint = new Paint();
        Paint circlePaint = new Paint();
        circlePaint.set(last_paint);
        circlePaint.setAlpha(20);
        Float[] last= new Float[]{0f,0f};
        for(Float[] position : lastInputHistory){
            if(first){
                first = false;
                canvas.drawCircle(position[0], position[1],10, last_paint);
                last = position;
                continue;
            }
            canvas.drawLine(last[0], last[1], position[0], position[1], linePaint);
            canvas.drawCircle(position[0], position[1],5, circlePaint);
            last = position;
        }
    }


    public void showGestureHistory(){
        showGestureHistory = true;
        invalidate();
    }
    private void drawTriangle(Canvas canvas, float x, float y, float sideLength, Paint paint ){
        float a = sideLength/2;
        float b = 1.73f/4 * sideLength;

        float[] top = new float[]{x,y-b};
        float[] left = new float[]{x-a,y+b};
        float[] right = new float[]{x+a,y+b};

        Paint.Style style = paint.getStyle();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(left[0], left[1]);
        path.lineTo(left[0], left[1]);
        path.lineTo(right[0], right[1]);
        path.lineTo(top[0], top[1]);
        path.close();
        canvas.drawPath(path, paint);

        paint.setStyle(style);
    }

    private void drawGestureHistory(Canvas canvas) {
        Paint currPaint = new Paint();
        currPaint.setAlpha(20);

        for (InputData inputData : gestureHistory) {

            switch (inputData.type) {
                case Flick:
                    int color  = getDirection(inputData.fromX, inputData.fromY, inputData.toX, inputData.toY).ordinal() % 2 == 0 ? Color.RED : Color.BLUE;
                    currPaint.setColor(color);
                    canvas.drawCircle(inputData.fromX,inputData.fromY,5, currPaint);
                    canvas.drawLine(inputData.fromX, inputData.fromY, inputData.toX, inputData.toY, currPaint);
                    //canvas.drawCircle(inputData.toX,inputData.toY,5, currPaint);
                    drawTriangle(canvas, inputData.toX, inputData.toY, 10, currPaint);
                    break;
                case Click:
                    currPaint.setColor(Color.GREEN);
                    canvas.drawCircle(inputData.toX,inputData.toY,5, currPaint);
                    break;
                case AFlick:
                    currPaint.setColor(Color.GRAY);
                    canvas.drawCircle(inputData.fromX,inputData.fromY,5, currPaint);
                    canvas.drawLine(inputData.fromX, inputData.fromY, inputData.toX, inputData.toY, currPaint);
                    //canvas.drawCircle(inputData.toX,inputData.toY,5, currPaint);
                    drawTriangle(canvas, inputData.toX,inputData.toY,10, currPaint);
                    break;
                case AClick:
                    currPaint.setColor(Color.BLACK);
                    canvas.drawCircle(inputData.toX,inputData.toY,5, currPaint);
                    break;
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
            canvas.drawCircle(mLastX, mLastY, S.getInstance().lastInputRadius, last_paint);
        }

        if(showGestureHistory){
            showGestureHistory =false;
            drawGestureHistory(canvas);
        }

        if(drawVIPosHistory){
            drawVIPosHistory = false;
            drawVIPosHistory(canvas);
        }


        canvas.drawLine(hoverX -100, hoverY ,hoverX +100, hoverY, last_paint );
        canvas.drawLine(hoverX, hoverY-100 ,hoverX, hoverY+100, last_paint );
    }


}
