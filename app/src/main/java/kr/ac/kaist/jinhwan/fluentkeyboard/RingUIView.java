package kr.ac.kaist.jinhwan.fluentkeyboard;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class RingUIView extends RelativeLayout implements OtherTouchListener {

    public enum KeyMode{
        V1, V2
    }

    private class FeedBackCircle{
        float radius;
    }

    private class DirToAlphabetViewMap extends HashMap<Direction, AlphabetView>{};

    int currKeySet = 0;
    private final int keySetSize = 2; //total key set, keyset, keyset2

    private final String[] _keySetERROR = {"-", "-", "-", "-", "-", "-", "-", "-"};
    private final String[][] _keySets = {{"<-", "ㅇ", "ㅅ", "ㄱ", "ㄴ", "ㄷ", "ㅈ", "ㅂ"},{"<-", "ㅁ", "ㅎ", "ㅋ", "ㄹ", "ㅌ", "ㅊ", "ㅍ"}};
    private final Integer[][] testColorSets = {{null,null,Color.argb(0xFF,0xFF,0x85,0x00),null,null,null,Color.argb(0xFF,0xFF,0x85,0x00),null},
            {null,null,Color.argb(0xFF,0xFF,0x85,0x00),null,null,null,Color.argb(0xFF,0xFF,0x85,0x00),null}};

    private double UI_SIZE;
    private double UI_SIZE_2;

    int centerX ;// = getX() + getWidth() / 2;
    int centerY ;//= getY() + getHeight() / 2;

    Paint paint = new Paint();
    ArrayList<ArrayList<AlphabetView>> ringLeafList = new ArrayList<>();
    DirToAlphabetViewMap[] dirToLeaf = {new DirToAlphabetViewMap(), new DirToAlphabetViewMap()};
    FeedBackView feedBackView;
    View widthView, heightView;

    private float mCurX;
    private float mCurY;
    private boolean isMoving;
    private float mDownX;
    private float mDownY;
    private boolean flick_valid;

    private float VIUIPro = 0.1f;
    private float hoverX;
    private float hoverY;
    private boolean hoverIn;


    AlphabetView currLeaf = null;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        double radian = 0;

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        UI_SIZE = 0.5 * getWidth()/2;
        UI_SIZE_2 = UI_SIZE * 1.5;

        RelativeLayout.LayoutParams wparams = (RelativeLayout.LayoutParams) widthView.getLayoutParams();
        wparams.width = (int) UI_SIZE_2;
        wparams.height =(int)(UI_SIZE_2 * 0.10);
        wparams.setMargins(centerX - wparams.width/2, (int)(centerY + UI_SIZE_2 * 0.75), 0,0);

        RelativeLayout.LayoutParams hparams = (RelativeLayout.LayoutParams) heightView.getLayoutParams();
        hparams.width = (int)(UI_SIZE_2 * 0.10);
        hparams.height =(int) UI_SIZE_2;
        hparams.setMargins((int)(centerX + UI_SIZE_2 * 0.75) , centerY - hparams.height/2, 0,0);


        //Log.e("ASDF", String.format("%f, %f, %d, %d", getX(), getY(), getWidth(), getHeight()));

        for(AlphabetView alphabetView : ringLeafList.get(0)){
            float childX = (float)(getWidth()/2 + UI_SIZE*Math.cos(radian) - alphabetView.getWidth()/2);
            float childY = (float)(getHeight()/2 + UI_SIZE*Math.sin(radian) - alphabetView.getHeight()/2);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) alphabetView.getLayoutParams();
            params.setMargins((int)childX, (int)childY, 0, 0);
            alphabetView.setOriginalPosition((int)childX, (int)childY);
            //Log.d("debug", ""+i);
            // Log.d("debug",String.format("alphabetView position %f %f" , alphabetView.getX(), alphabetView.getY())  );

            radian -= Math.PI / 4;
        }

        for(AlphabetView alphabetView : ringLeafList.get(1)){
            float childX = (float)(getWidth()/2 + UI_SIZE_2*Math.cos(radian) - alphabetView.getWidth()/2);
            float childY = (float)(getHeight()/2 + UI_SIZE_2*Math.sin(radian) - alphabetView.getHeight()/2);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) alphabetView.getLayoutParams();
            params.setMargins((int)childX, (int)childY, 0, 0);
            alphabetView.setOriginalPosition((int)childX, (int)childY);

            alphabetView.animate().scaleX(0.7f).scaleY(0.7f).start();
//            alphabetView.setScaleX(0.7f);
//            alphabetView.setScaleY(0.7f);

            radian -= Math.PI / 4;
        }

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public RingUIView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(6f);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);


        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        feedBackView = (FeedBackView)inflater.inflate(R.layout.feedback_circle_view,null);
        this.addView(feedBackView);
        widthView = new View(context);
        widthView.setBackgroundResource(R.drawable.fill_all);
        widthView.setAlpha(S.getInstance().originalAlpha);
        this.addView(widthView);
        heightView = new View(context);
        heightView.setBackgroundResource(R.drawable.fill_all);
        heightView.setAlpha(S.getInstance().originalAlpha);
        this.addView(heightView);


        //Log.e("ASDF", String.format("%f, %f, %d, %d", centerX, centerY, getWidth(), getHeight()));
        double radian = 0;
        for(int keySetNum = 0; keySetNum < keySetSize ; keySetNum++) {
            ringLeafList.add(new ArrayList<AlphabetView>());
            for (int i = 0; i < _keySets[keySetNum].length; i++) {

                View view = inflater.inflate(R.layout.alphabet_view, null);
                AlphabetView alphabetView = (AlphabetView) view;
                alphabetView.setText(_keySets[keySetNum][i]);

                if(testColorSets[keySetNum][i]!=null){
                    alphabetView.setTextColor(testColorSets[keySetNum][i]);
                }

                this.addView(alphabetView);
                ringLeafList.get(keySetNum).add(alphabetView);
                dirToLeaf[keySetNum].put(Direction.values()[i], alphabetView); //E,NE ...
            }
        }


    }

    public void changeSet(KeyMode keyMode){
        switch(keyMode){
            case V1:
                currKeySet = 0;
                break;
            case V2:
                currKeySet = 1;
                break;
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("onDraw", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));

        paint.setColor(Color.RED);
        canvas.drawCircle(getWidth()/2, getHeight()/2 , 1, paint);

        if(S.getInstance().hoverTrack) {
            paint.setColor(hoverIn ? Color.MAGENTA : Color.GRAY);
            float VIUISize = VIUIPro * getHeight();
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, VIUISize, paint);

            paint.setColor(Color.MAGENTA);
            float length = VIUISize / 2;
            canvas.drawLine(hoverX - length, hoverY, hoverX + length, hoverY, paint);
            canvas.drawLine(hoverX, hoverY - length, hoverX, hoverY + length, paint);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false; // this view is transparent for events.
    }


    public void OnHoverOnVI(MotionEvent event ,float VIX, float VIY){
        float VIUISize = VIUIPro * getHeight();
        float radius = S.getInstance().getLastInputRadius();
        float transPro = VIUISize/radius;

        hoverIn = Math.sqrt(Math.pow(event.getX()-VIX, 2) + Math.pow(event.getY()-VIY, 2)) < S.getInstance().getLastInputRadius();

        hoverX = (event.getX() - VIX) * transPro + getWidth()/2;
        hoverY = (event.getY() - VIY )* transPro + getHeight()/2;

        invalidate();
    }

 /*   private boolean fixMovement = false;
    public void fixMovement(){
        fixMovement = true;
        for(AlphabetView av : ringLeafList.get(currKeySet)){
            if(av != currLeaf){
                av.recoverToOriginal();
            }else{
                currLeaf.select();
                int pX = (int) ((centerX - currLeaf.getWidth() / 2) );
                int pY = (int) ((centerY - currLeaf.getHeight() / 2));
                currLeaf.moveToPositionByAnimation(pX, pY);
            }
        }
    }
    public void releaseMovement(){
        fixMovement = false;
        for(AlphabetView av : ringLeafList.get(currKeySet)){
            av.recoverToOriginal();
        }
    }
*/
    public void setColorDir(Direction dir , int color){
        for(DirToAlphabetViewMap map : dirToLeaf){
            map.get(dir).getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
    }

    public void setColorDir(Direction dir , int color, int keyMode){
        dirToLeaf[keyMode].get(dir).getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public void setOuterRingColor(int color){
/*        FeedBackCircle newCircle = new FeedBackCircle(getSmallerSide());
        feedBackCircleList.add(newCircle);
        ObjectAnimator.ofFloat(newCircle, "radius", getSmallerSide(), getSmallerSide() *1.2f).setDuration(500).start();
        invalidate();*/
        /*Animation feedbackAnim= AnimationUtils.loadAnimation(getContext(), R.anim.ring_feedback);
        feedBackView.startAnimation(feedbackAnim);
*/
/*        StateListDrawable background = (StateListDrawable) feedBackView.getBackground();
        TransitionDrawable td = (TransitionDrawable) background.getCurrent();
        td.startTransition(2000);*/
        feedBackView.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }


    public void setSelection(int keyMode, Direction dir){
        Log.d("ring_selection", "select : " + dir);
        AlphabetView curr = dirToLeaf[keyMode].get(dir);
        //curr.moveToPositionByAnimation(centerX - curr.getWidth()/2, centerY - curr.getHeight()/2 );
        curr.select();
    }


    public void setSelectionDisable(int keyMode, Direction dir) {
        Log.d("ring_selection", "deselect : " + dir);
        dirToLeaf[keyMode].get(dir).recoverToOriginal();
    }

    public void setColorWidth(int color){
        widthView.setBackgroundColor(color);
        //widthView.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public void setColorHeight(int color){
        heightView.setBackgroundColor(color);
        //heightView.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }


    private class MoveData{
        float x,y;
        long inputTime;
        MoveData(float x, float y, long inputTime){
            this.x = x;
            this.y = y;
            this.inputTime =inputTime;
        }
    }

    LinkedList<MoveData> moveHistory = new LinkedList<>();

    @Override
    public boolean otherOnTouchEvent(MotionEvent e) {
     /*   if(fixMovement){
            return false;
        }
*/
       /* Log.v("RingUI", "otherOnTouchEvent");
        float x = e.getX();
        float y = e.getY();

        mCurX =x;
        mCurY =y;

        isMoving = false;

        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
                //moveHistory = new LinkedList<>();
                //moveHistory.add(new MoveData(mCurX, mCurY, System.currentTimeMillis()));

                mDownX = x;
                mDownY = y;
                flick_valid = false;

                //Log.d("inputFiledView", "down");
                break;
            case MotionEvent.ACTION_MOVE:
                //moveHistory.add(new MoveData(mCurX, mCurY, System.currentTimeMillis()));

                isMoving = true;
                double flickLength = Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2));
                flick_valid = flickLength > S.getInstance().getMinFlickRadius();

                if(flick_valid) {
                    Direction dir = Direction.getOpposite(Direction.getDirection(mDownX, mDownY, x, y));
                    AlphabetView leaf = dirToLeaf[currKeySet].get(dir);

                    // curr leaf should be changed?
                    if (currLeaf != leaf && currLeaf != null) {
                        //curr leaf should be changed
                        currLeaf.recoverToOriginal();
                    }
                    currLeaf = leaf;

                    if(flickLength > S.getInstance().getValidFlickRadius() && !currLeaf.isSelected()){
                        float progress = 1f;
                        int pX = (int) (progress * (centerX - leaf.getWidth() / 2) + (1 - progress) * leaf.getOriginalX());
                        int pY = (int) (progress * (centerY - leaf.getHeight() / 2) + (1 - progress) * leaf.getOriginalY());
                        Log.d("RingUI", String.format("move to x=%d, y=%d", (int) pX, (int) pY));
                        currLeaf.moveToByInstant(pX, pY);
                        currLeaf.select();
                    }

                }else{
                    if(currLeaf != null){
                        currLeaf.recoverToOriginal();
                    }
                    currLeaf = null;
                }

                break;
            case MotionEvent.ACTION_UP:
                if(currLeaf != null){
                    //currLeaf.recoverToOriginal();
                    Log.d("RingUI", String.format("move, action up x=%d, y=%d", (int) centerX, (int) centerY));
                    currLeaf.recoverToOriginal();
                }
                break;
        }*/



        return false;
    }
}
