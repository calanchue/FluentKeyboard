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

import static kr.ac.kaist.jinhwan.fluentkeyboard.Direction.getDirection;
import static kr.ac.kaist.jinhwan.fluentkeyboard.Direction.getDirection4;
import static kr.ac.kaist.jinhwan.fluentkeyboard.InputHistory.InputData;


public class InputFieldView extends ViewGroup {

    private float mCurX, mCurY, mDownX, mDownY, m_VIX, m_VIY;

    private float UI_SIZE = S.getInstance().getMaxFlickRadius();

    private Paint boldBlackPaint = new Paint();
    private Paint boldMagentaPaint = new Paint();
    private Paint boldBluePaint = new Paint();
    private Paint bluePaint = new Paint();
    private Paint redPaint = new Paint();
    private Paint blackPaint = new Paint();
    private Paint drawBentPaint = new Paint();

    private boolean flick_valid = false;
    private int keyMode = 0;
    private int MAX_KEY_PAD =2;
    private int keyPadInterval = 300;
    private long lastAClickTime = 0;

    private long lastDownTime = 0;

    private boolean isMoving, isLastInput =false;

    public RingUIView ringUIView;

    private boolean showGestureHistory =false;

    private final static Direction backSpaceDir = Direction.W;

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
        boldBlackPaint.setColor(Color.BLACK);
        boldBlackPaint.setStrokeWidth(6f);
        boldBlackPaint.setStrokeJoin(Paint.Join.ROUND);
        boldBlackPaint.setStyle(Paint.Style.STROKE);

        boldMagentaPaint.set(boldBlackPaint);
        boldMagentaPaint.setColor(Color.MAGENTA);

        boldBluePaint.set(boldBlackPaint);
        boldBluePaint.setColor(Color.BLUE);

        blackPaint.setStyle(Paint.Style.STROKE);
        bluePaint.set(blackPaint);
        bluePaint.setColor(Color.BLUE);
        redPaint.set(blackPaint);
        redPaint.setColor(Color.RED);





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
                    ringUIView.OnHoverOnVI(event, m_VIX, m_VIY);
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
       printText(s);
    }


    InputHistory gestureHistory = new InputHistory();
    LinkedList<InputData> recentInputHistory = new LinkedList<>();
    //List<Float[]> VIInputHistory = new ArrayList<>();



    public void setLastInput(float x, float y ,
                             boolean isInputFromA, boolean isFlick){
        Log.d("InputField", String.format("try to set last input to %f,%f", x, y));

        float totalX = 0;
        float totalY = 0;
        switch (S.getInstance().inputOption){
            case Fixed:
                if(m_VIX == 0 && m_VIY == 0){

                    m_VIX = x;
                    m_VIY = y;
                    //Log.d("InputField", String.format("last input changed  to %f,%f", x, y));
                }
                break;
            case Last:
                m_VIX = x;
                m_VIY = y;
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
                m_VIX = totalX/ recentInputHistory.size();
                m_VIY = totalY/ recentInputHistory.size();
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
                m_VIX = totalX/ recentInputHistory.size();
                m_VIY = totalY/ recentInputHistory.size();
                break;
            case AdaptVowel:
                //only gather data from Input converges to center
                if(recentInputHistory.isEmpty()){
                    InputData recentInputData = gestureHistory.get(gestureHistory.size() - 1);
                    recentInputHistory.add(recentInputData);
                    //must me consonant
                    m_VIX = recentInputData.toX;
                    m_VIY = recentInputData.toY;
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
                m_VIX = totalX/ recentInputHistory.size();
                m_VIY = totalY/ recentInputHistory.size();
                break;
        }
/*        if(S.getInstance().inputOption == S.InputOption.Fixed){
            if(m_VIX == 0 && m_VIY == 0){
                m_VIX = x;
                m_VIY = y;
                Log.d("InputField", String.format("last input changed  to %f,%f", x, y));
            }
        }else if(S.getInstance().inputOption.Last{
            m_VIX = x;
            m_VIY = y;
        }*/
    }

    private boolean isInRadius(float x, float y,float  r_x, float r_y, float radius){
        return Math.sqrt(Math.pow(x-r_x,2) + Math.pow(y-r_y,2)) < radius;
    }

    boolean outMinFlickRadius = false;
    boolean VIOrigin;

    float[] VIExitPos;
    boolean VIExit =false, VIIn = false, prevIn =false;
    int inBackCount;
    Direction inDirection = null;
    boolean doubleConsonant = false;

    private final static long ORIGINAL_BS_INTERVAL = 500;
    private final static long MIN_BS_INTERVAL = 100;
    private final static long BS_INTERVAL_DECREASE = 100;
    long currBSInterval;
    long lastBSTime;

    Direction startDirection;

    //mesure bending
    boolean angleDeltaCountStart;
    double angleDeltaSum;
    float travelLengthAfterVI;
    ArrayList<float[]>  clearFlickHistory= new ArrayList<>();
    LinkedList<float[]>  bentPosition = new LinkedList<>();


    //measure key2 bending
    boolean reverseBefore =false;
    float[] prevPos = new float[]{0f,0f};
    Float[] key2BentPos;
    boolean outMinBent2Radius = false;


    ArrayList<float[]>  oneFlickHistory = new ArrayList<>();

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        mCurX= e.getX();
        mCurY= e.getY();

        isMoving = false;

        switch (e.getAction()) {


            case MotionEvent.ACTION_DOWN:
                if(System.currentTimeMillis() - lastAClickTime < keyPadInterval){
                    keyMode = 1;
                    ringUIView.changeSet(RingUIView.KeyMode.V2);
                    ringUIView.setOuterRingColor(Color.DKGRAY);
                }


                //initializing
                VIOrigin = Math.sqrt(Math.pow(m_VIX -mCurX,2) + Math.pow(m_VIY -mCurY,2)) < S.getInstance().getLastInputRadius();
                mDownX = mCurX;
                mDownY = mCurY;
                flick_valid = false;
                lastDownTime = System.currentTimeMillis();
                outMinFlickRadius = false;
                VIExit = false;
                VIIn = false;
                inDirection = null;
                while (!oneFlickHistory.isEmpty()) {
                    oneFlickHistory.remove(oneFlickHistory.size()-1);
                }
                prevIn = false;
                inBackCount =0;
                doubleConsonant= false;
                currBSInterval = ORIGINAL_BS_INTERVAL;
                lastBSTime = 0;

                //benting init
                angleDeltaSum = 0;
                angleDeltaCountStart = false;
                travelLengthAfterVI= 0;
                clearFlickHistory.clear();
                clearFlickHistory.add(new float[]{mCurX, mCurY});
                bentPosition.clear();

                //benting2
                reverseBefore = false;
                key2BentPos = null;
                outMinBent2Radius = false;

                //fix property
                startDirection = getDirection(m_VIX, m_VIY, mDownX, mDownY);

                //do something
                ringUIView.setColorDir(startDirection, Color.BLUE, keyMode);

                //Log.v("bent2", String.format("############## new Input"));
                //Log.d("inputFiledView", "down");
                break;
            case MotionEvent.ACTION_MOVE:
                isMoving = true;
                if(!outMinBent2Radius){
                    if(Math.sqrt(Math.pow(mDownX-mCurX,2) + Math.pow(mDownY-mCurY,2)) > S.getInstance().bent2MinFlickRadius) {
                        outMinBent2Radius = true;
                    }
                }else{
                    if(!VIIn) {//after go into VI, bent2 will not be processed, bent1 will be processed;
                        //calculate bent2
                        if (!reverseBefore) {
                            //Log.v("bent2", String.format("=============== mCur(%f,%f)", mCurX,mCurY));
                            boolean reversBeforeTest = Direction.sameField(prevPos[0], prevPos[1], mCurX, mCurY, startDirection);
                            //Log.v("bent2", String.format("reverseBeforeTest = %b", reversBeforeTest));
                            if (reversBeforeTest) {
                                //Log.v("bent2", "reverseBefore=true");
                                reverseBefore = true;
                            }
                        } else {
                            if (key2BentPos == null) {
                                //Log.v("bent2", String.format("=============== mCur(%f,%f)", mCurX,mCurY));
                                boolean reverseToNormal = !Direction.sameField(prevPos[0], prevPos[1], mCurX, mCurY, startDirection);
                                Log.v("bent2", String.format("reverseToNormal = %b", reverseToNormal));
                                if (reverseToNormal) {
                                    boolean isInStartRadius = isInRadius(prevPos[0], prevPos[1], mDownX, mDownY, S.getInstance().getMinFlickRadius());
                                    boolean isInOppositeFiled = Direction.sameField(mDownX, mDownY, prevPos[0], prevPos[1], startDirection);
                                    Log.v("bent2", String.format("isInStartRadius= %b, isInOppositeField = %b", isInStartRadius, isInOppositeFiled));
                                    if (isInStartRadius || isInOppositeFiled) {
                                        key2BentPos = new Float[]{prevPos[0], prevPos[1]};
                                        Log.v("bent2", String.format("new key2BentPos : %s", key2BentPos));
                                        ringUIView.setSelectionDisable(0, startDirection);
                                        ringUIView.setColorDir(startDirection, Color.WHITE, 0);
                                        keyMode = 1;
                                        ringUIView.changeSet(RingUIView.KeyMode.V2);
                                        ringUIView.setColorDir(startDirection, Color.BLUE, 1);
                                        ringUIView.setSelection(1, startDirection);
                                        ringUIView.setOuterRingColor(Color.DKGRAY);
                                    }
                                }
                            }
                        }
                    }
                }


                //dooing
                if (!outMinFlickRadius){ //never out minflick before
                    if(Math.sqrt(Math.pow(mDownX-mCurX,2) + Math.pow(mDownY-mCurY,2)) > S.getInstance().getMinFlickRadius()){
                        outMinFlickRadius = true;
                        ringUIView.setSelection(keyMode, startDirection);
                    }else{
                        if (System.currentTimeMillis() - lastDownTime > S.getInstance().longPressInterval) {
                            //force last position
                            while(!recentInputHistory.isEmpty()){
                                recentInputHistory.removeFirst();
                            }
                            for (int i = 0; i < S.getInstance().adaptHistorySize; i++) {
                                recentInputHistory.add(new InputData(mCurX, mCurY));
                            }
                            m_VIX = mCurX;
                            m_VIY = mCurY;
                        }
                    }
                }else {//after minFlick radius
                    float[] prevPos = clearFlickHistory.get(clearFlickHistory.size() - 1);
                    float currLength = (float) Math.sqrt(Math.pow(mCurX - prevPos[0], 2) + Math.pow(mCurY - prevPos[1], 2));
                    //if (currLength > travelLengthAfterVI / clearFlickHistory.size() / 2) {
                    if(true){
                        travelLengthAfterVI += currLength;
                        clearFlickHistory.add(new float[]{mCurX, mCurY});
                        if (angleDeltaCountStart && clearFlickHistory.size() >= 3) {
                            float[] prev2Pos = clearFlickHistory.get(clearFlickHistory.size() - 3);

                            float dx1 = prevPos[0] - prev2Pos[0];
                            float dy1 = prevPos[1] - prev2Pos[1];
                            float dx2 = mCurX - prevPos[0];
                            float dy2 = mCurY - prevPos[1];

                            float d = dx1 * dx2 + dy1 * dy2;   // dot product of the 2 vectors
                            float l2 = (dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2); // product of the squared lengths

                            double deltaAngle = Math.acos(d / Math.sqrt(l2));
                            double __tempAD2 = angleDeltaSum;
                            if(deltaAngle ==Double.NaN){
                                deltaAngle = 0;
                            }
                            angleDeltaSum += Math.abs(deltaAngle);

                            Log.v("AngleDelta", String.format("angleDeltaSum=%f -> %f (+ %f)", __tempAD2, angleDeltaSum, Math.abs(deltaAngle)));
                            if (angleDeltaSum > Math.PI) {
                                double __tempAD = angleDeltaSum;
                                angleDeltaSum -= Math.PI;
                                Log.v("AngleDelta", String.format("BENT angleDeltaSum=%f -> %f", __tempAD, angleDeltaSum));
                                bentPosition.add(new float[]{mCurX, mCurY});
                                if(bentPosition.size() > 1){
                                    if(startDirection == Direction.E){//bs direciton
                                        messageListener.listenMessage(MessageListener.Type.special, "bs");
                                    }else {
                                        doubleConsonant = true;
                                        ringUIView.setOuterRingColor(Color.BLUE);
                                    }
                                }
                            }
                        }
                    }//else throw away
                }

                // Does it travel across VI?
                if(!VIIn && !VIOrigin){
                    VIIn = isInRadius(mCurX,mCurY, m_VIX, m_VIY, S.getInstance().getLastInputRadius());
                    if(VIIn){//first approach to VI
                        if(S.getInstance().inDirFromStartPos){
                            inDirection = Direction.getDirection(mDownX,mDownY,m_VIX, m_VIY);
                        }else {
                            inDirection = Direction.getDirection(mDownX, mDownY, mCurX, mCurY);
                        }
                        //angle measure start;
                        angleDeltaCountStart = true;

                        //ringUIView.fixMovement();
                        prevIn = true;
                    }
                }else if(VIIn && !VIOrigin){
                    boolean currIn = isInRadius(mCurX,mCurY, m_VIX, m_VIY, S.getInstance().getLastInputRadius());
                    if(prevIn){
                        //it goes out from VI!
                        if(currIn == false){
                            Direction dir4 = Direction.getDirection4(m_VIX, m_VIY, mCurX, mCurY);
                            if(dir4 == Direction.N || dir4 == Direction.S){
                                ringUIView.setColorHeight(Color.BLUE);
                            }else{
                                ringUIView.setColorWidth(Color.BLUE);
                            }
                            //ringUIView.setOuterRingColor(Color.BLUE);
                            prevIn = false;
                        }
                    }else {
                        //it is in VI
                        if(inDirection == backSpaceDir){
                            /*if(System.currentTimeMillis() -  lastBSTime > currBSInterval){
                                messageListener.listenMessage(MessageListener.Type.special, "bs");
                                lastBSTime = System.currentTimeMillis();
                                currBSInterval = currBSInterval - BS_INTERVAL_DECREASE;
                                currBSInterval = currBSInterval > MIN_BS_INTERVAL ? currBSInterval : MIN_BS_INTERVAL;
                            }*/
                        }
                        // in VI again!
                        if(currIn == true){
                            ringUIView.setColorHeight(Color.WHITE);
                            ringUIView.setColorWidth(Color.WHITE);
                            //obsolte
/*                            Direction inBackDir = Direction.getDirection(mCurX, mCurY, m_VIX, m_VIY);
                            if(inBackDir == inDirection){
                                doubleConsonant = true;
                            }else {
                                inBackCount++;
                            }*/
                            prevIn = true;
                        }
                    }
                }
                prevPos = new float[]{mCurX,mCurY};
                Log.d("bent2", String.format("prevPos(%f, %f)", prevPos[0],prevPos[1]));

                break;
            case MotionEvent.ACTION_UP:

                if (VIOrigin) {
                    // starting point == LastInputCircle
                    if (Math.sqrt(Math.pow(mDownX - mCurX, 2) + Math.pow(mDownY - mCurY, 2)) > S.getInstance().getMinFlickRadius()) {
                        //AFlick
                        //printDirection("<font color='magenta'>Flick</font>");
                        Direction dir = getDirection4(mDownX, mDownY, mCurX, mCurY);
                        printDirection(String.format("<font color='magenta'>%s</font>,", dir.toString()));
                        printText(mapInputToKey(keyPadType.M, dir,false));
                        isLastInput = true;


                        gestureHistory.add(new InputData(mDownX, mDownY, mCurX, mCurY, m_VIX, m_VIY, InputData.GestureType.AFlick));
                        setLastInput(mCurX, mCurY, true, true);
                    } else {
                        //AClick
                        printDirection("<font color='magenta'>Click</font>");
                        printText(mapInputToKey(keyPadType.M, Direction.NON,false));
                        isLastInput = true;


                        gestureHistory.add(new InputData(mDownX, mDownY, mCurX, mCurY, m_VIX, m_VIY, InputData.GestureType.AClick));
                        setLastInput(mCurX, mCurY, true, false);
                    }
                } else {
                    if (outMinFlickRadius) {
                        processFlick();
                    } else {
                        // normal click or press end
                        lastAClickTime = System.currentTimeMillis();
                        printDirection("A_click ");
                        //keyMode = ++keyMode % MAX_KEY_PAD;

                        isLastInput = true;
                    }
                }

                isMoving =false;


                ringUIView.setOuterRingColor(Color.WHITE);
                //ringUIView.releaseMovement();
                ringUIView.changeSet(RingUIView.KeyMode.V1);
                ringUIView.setColorDir(startDirection, Color.WHITE);
                ringUIView.setSelectionDisable(keyMode, startDirection);
                ringUIView.setColorWidth(Color.WHITE);
                ringUIView.setColorHeight(Color.WHITE);
                keyMode = 0;

                break;
        }

        oneFlickHistory.add(new float[]{mCurX, mCurY});

        invalidate();

        //Log.d("inputFiledView", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));
        ringUIView.otherOnTouchEvent(e);

        return true;
    }

    private void processFlick(){
        boolean inputEndInVI = isInRadius(mCurX,mCurY, m_VIX, m_VIY, S.getInstance().getLastInputRadius());
        if(VIIn && !inputEndInVI){
            dirToConsonantOut(inDirection, doubleConsonant);

            //back space doesn't need vowel
            if(inDirection != backSpaceDir){
                int dotNum = inBackCount %2;
                for(int i = 0; i < dotNum ;i++){
                    printText(mapInputToKey(keyPadType.M, Direction.NON,false));
                }

                //inDirection;
                Direction vowelDirection = Direction.getDirection4(m_VIX, m_VIY,mCurX, mCurY);

                printText(mapInputToKey(keyPadType.M,vowelDirection,false));
            }

        }else if(VIIn && inputEndInVI){
            dirToConsonantOut(inDirection,doubleConsonant);
        }
        else {
            //just normal flick
            // normal flick end
            Direction dir = getDirection(mDownX, mDownY, mCurX, mCurY);
            Direction dir4 = getDirection4(mDownX,mDownY,mCurX,mCurY);
            boolean consumed = false;

            // Direction it started
            Direction startDir4 = getDirection4(m_VIX, m_VIY, mDownX, mDownY);
            switch (startDir4) {
                case N:
                    if(dir4 == Direction.E) {
                        //space
                        consumed = true;
                        printText(" ");
                    }
                    break;
                case S:
                    if(dir4 == Direction.E) {
                        //space
                        consumed = true;
                        printText(" ");
                    }
                    break;
                case E:
                    if(dir4 == Direction.S){
                        //enter
                        consumed = true;
                        printText("\n");
                    }
                    break;
                case W:
                    if(dir4 == Direction.S){
                        //enter
/*                        consumed = true;
                        printText("\n");*/
                    }
                    break;
            }
            if(!consumed) {


                if(S.getInstance().inDirFromStartPos){
                    Direction startToCenterDir = Direction.getDirection(mDownX, mDownY, m_VIX, m_VIY);
                    dirToConsonantOut(startToCenterDir,false);
                }else{
                    dirToConsonantOut(dir,false);
                }
                isLastInput = true;


                gestureHistory.add(new InputData(mDownX, mDownY, mCurX, mCurY, m_VIX, m_VIY, InputData.GestureType.Flick));
                setLastInput(mCurX, mCurY, false, true);
            }
        }
    }

    private void dirToConsonantOut(Direction dir, boolean doubleConsonant){
        if (dir != backSpaceDir) {
            if (keyMode == 0) {
                printText(mapInputToKey(keyPadType.J1, dir, doubleConsonant));
                printDirection(dir.toString());
            } else if (keyMode == 1) {
                printText(mapInputToKey(keyPadType.J2, dir, doubleConsonant));
                printDirection(String.format("<font color='blue'>%s</font>,", dir.toString()));
            }
        } else if (dir == backSpaceDir) {
            if (keyMode == 0) {
                if(currBSInterval != ORIGINAL_BS_INTERVAL){//drag bs happened
                    //already bs by dragging
                }else {
                    Log.d("inputfieldview", "bs");
                    messageListener.listenMessage(MessageListener.Type.special, "bs");
                }
            } else if (keyMode == 1) {

            }
        }
    }


    private String mapInputToKey(keyPadType k, Direction d, boolean doubleConsonant){
        if(k == keyPadType.J1){
            switch (d){
                case E:
                    return doubleConsonant ? "ㅃ" : "ㅂ";
                case NE:
                    return doubleConsonant ? "ㄸ" : "ㄷ";
                case N:
                    return "ㄴ";
                case NW:
                    return doubleConsonant ? "ㄲ" : "ㄱ";
                case W:
                    //backsapce
                    return " ";
                case SW:
                    return "ㅇ";
                case S:
                    return doubleConsonant ? "ㅉ" : "ㅈ";
                case SE:
                    return doubleConsonant ? "ㅆ" : "ㅅ";
            }
        }else if(k == keyPadType.J2){
            switch (d){
                case E:
                    return "ㅍ";
                case NE:
                    return "ㅌ";
                case N:
                    return "ㄹ";
                case NW:
                    return "ㅋ";
                case W:
                    //bs
                    return " ";
                case SW:
                    return "ㅁ";
                case S:
                    return "ㅊ";
                case SE:
                    return "ㅎ";
            }
        }else if(k == keyPadType.M){
            switch (d){
                case E:
                case W:
                    return "ㅡ";
                case N:
                case S:
                    return "ㅣ";
                case NON:
                    return "·";
            }
        }
        return "ERR";
    }

    private void drawGuide(Canvas canvas, int xOff, int yOff){
        if(isMoving) {
            canvas.drawLine(mDownX+xOff, mDownY+yOff, mCurX+xOff, mCurY+yOff, flick_valid ? boldMagentaPaint : boldBlackPaint);
            canvas.drawCircle(mDownX+xOff,mDownY+yOff, S.getInstance().getMinFlickRadius(),flick_valid ? boldMagentaPaint : boldBlackPaint);
            for(Coord coord :UIPoints){
                canvas.drawLine(mDownX+xOff, mDownY+yOff, mDownX+coord.x+xOff, mDownY+coord.y+yOff, keyMode ==0 ? boldBlackPaint : boldBluePaint);
            }
        }
    }

    private void drawOneFlick(Canvas canvas, Direction toDir){
        if(oneFlickHistory.isEmpty()){
            return;
        }
        Paint prevPaint = null;

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        float[] prevPoint = oneFlickHistory.get(0);
        path.moveTo(prevPoint[0], prevPoint[1]);
        Paint currPaint = blackPaint;
        for(float[] input : oneFlickHistory){
/*            currPaint = getDirection(mDownX,mDownY,input[0], input[1]).ordinal() %2==0 ?  redPaint : bluePaint;
            if(prevPaint != currPaint){
                //path.close();
                canvas.drawPath(path, currPaint);
                path = new Path();
                canvas.drawLine(prevPoint[0], prevPoint[1],input[0], input[1], blackPaint);
                path.moveTo(input[0], input[1]);
                prevPaint = currPaint;
            }
            path.lineTo(input[0], input[1]);
            canvas.drawLine(mDownX,mDownY,input[0], input[1], currPaint );*/

            if(toDir != null){
                currPaint = getDirection(prevPoint[0], prevPoint[1], input[0],input[1]).ordinal() %2==0 ?  redPaint : bluePaint;
            }else{

            }



            canvas.drawLine(prevPoint[0], prevPoint[1], input[0], input[1], currPaint);


            prevPoint = input;
        }
        //path.close();
        canvas.drawPath(path, currPaint);
    }

    private void drawBentingHistory(Canvas canvas){
        if(clearFlickHistory.isEmpty()){
            return;
        }
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        float[] prevPoint = clearFlickHistory.get(0);
        path.moveTo(prevPoint[0], prevPoint[1]);
        Paint currPaint = drawBentPaint;
        currPaint.setAlpha(100);
        for(float[] input : clearFlickHistory){
            canvas.drawLine(prevPoint[0]-100, prevPoint[1], input[0]-100, input[1], currPaint);
            prevPoint = input;
        }
        for(float[] input : bentPosition){
            canvas.drawCircle(input[0]-100, input[1], 5, currPaint);
            canvas.drawCircle(input[0], input[1], 10, currPaint);
        }

        //path.close();
        //canvas.drawPath(path, currPaint);
    }


    private void drawTimeLastInputChangeGraph(Canvas canvas){
        int startX = getWidth()/2;;
        int startY = getWidth()/2;
        int height = 100;
        int width = 200;



        boolean isFirst =true;
        LinkedList<Double> distanceList = new LinkedList<>();
        float[] prev = {0f,0f};
        Double distanceMax =0.0;
        for(InputData inputData: gestureHistory){
            float[] lastInput = new float[]{inputData.VIX, inputData.VIY};
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



    public void showGestureHistory(){
        showGestureHistory = true;
        invalidate();
    }


    public InputHistory getInputHistory(){
        return gestureHistory;
    }
    public void saveInputHistory(String file_path){
        InputHistory.save(file_path, gestureHistory);
    }

    public void loadInputHistory(String file_path){
        InputHistory load = InputHistory.load(file_path);
        if(load == null ){
        }else {
            gestureHistory = load;
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("onDraw", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));

        drawGuide(canvas,0,0);//main guide
        drawGuide(canvas, -300,-300);// offset guid



        if(isLastInput){
            canvas.drawCircle(m_VIX, m_VIY, S.getInstance().getLastInputRadius(), boldMagentaPaint);
        }

        if(showGestureHistory){
            showGestureHistory =false;
            gestureHistory.drawGestureHistory(canvas);
        }

        if(drawVIPosHistory){
            drawVIPosHistory = false;
            gestureHistory.drawVIPosHistory(canvas);
        }


        drawOneFlick(canvas,startDirection);
        drawBentingHistory(canvas);

        if(key2BentPos != null){
            canvas.drawCircle(key2BentPos[0],key2BentPos[1],10, redPaint );
            canvas.drawCircle(mDownX,mDownY, S.getInstance().bent2MinFlickRadius, blackPaint);
        }

        canvas.drawLine(hoverX -100, hoverY ,hoverX +100, hoverY, boldMagentaPaint);
        canvas.drawLine(hoverX, hoverY-100 ,hoverX, hoverY+100, boldMagentaPaint);
    }


}
