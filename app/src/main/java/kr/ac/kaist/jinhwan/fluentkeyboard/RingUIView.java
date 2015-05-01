package kr.ac.kaist.jinhwan.fluentkeyboard;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.LinkedList;

public class RingUIView extends RelativeLayout implements OtherTouchListener {

    private String[] _keySet = {"<-", "ㅇ", "ㅈ", "ㅅ", "ㅂ", "ㄷ", "ㄴ", "ㄱ"};
    private String[] _keySet2 = {"<-", "ㅁ", "ㅊ", "ㅎ", "ㅠ", "ㅌ", "ㄷ", "ㅋ"};

    private double UI_SIZE = 100;

    int centerX ;// = getX() + getWidth() / 2;
    int centerY ;//= getY() + getHeight() / 2;

    Paint paint = new Paint();
    LinkedList<AlphabetView> ringLeafList = new LinkedList<>();
    HashMap<Direction, AlphabetView> dirToLeaf = new HashMap<>();

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

        UI_SIZE = 0.6 * getWidth()/2;


        //Log.e("ASDF", String.format("%f, %f, %d, %d", getX(), getY(), getWidth(), getHeight()));

        for (int i = 0; i < _keySet.length; i++) {
            AlphabetView alphabetView = ringLeafList.get(i);
            float childX = (float)(getWidth()/2 + UI_SIZE*Math.cos(radian) - alphabetView.getWidth()/2);
            float childY = (float)(getHeight()/2 + UI_SIZE*Math.sin(radian) - alphabetView.getHeight()/2);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) alphabetView.getLayoutParams();
            params.setMargins((int)childX, (int)childY, 0, 0);
            alphabetView.setOriginalPosition((int)childX, (int)childY);
            //Log.d("debug", ""+i);
           // Log.d("debug",String.format("alphabetView position %f %f" , alphabetView.getX(), alphabetView.getY())  );

            radian -= Math.PI / 4;
        }

        //invalidate();
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


        //Log.e("ASDF", String.format("%f, %f, %d, %d", centerX, centerY, getWidth(), getHeight()));
        double radian = 0;
        for (int i = 0; i < _keySet.length; i++) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.alphabet_view, null);
            AlphabetView alphabetView = (AlphabetView) view;
            alphabetView.setText(_keySet[i]);

            this.addView(alphabetView);
            ringLeafList.add(alphabetView);
            dirToLeaf.put(Direction.values()[i], alphabetView); //E,NE ...

        }
    }

    public void changeSet(){
        
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("onDraw", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));

        paint.setColor(Color.RED);
        canvas.drawCircle(getWidth()/2, getHeight()/2 , 1, paint);

        paint.setColor(hoverIn?Color.MAGENTA : Color.GRAY);
        float VIUISize = VIUIPro * getHeight();
        canvas.drawCircle(getWidth()/2, getHeight()/2, VIUISize , paint);

        paint.setColor(Color.MAGENTA);
        float length = VIUISize/2;
        canvas.drawLine(hoverX -length, hoverY ,hoverX +length, hoverY, paint );
        canvas.drawLine(hoverX, hoverY-length ,hoverX, hoverY+length, paint );

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false; // this view is transparent for events.
    }


    public void OnHoverOnVI(MotionEvent event ,float VIX, float VIY){
        float VIUISize = VIUIPro * getHeight();
        float radius = S.getInstance().lastInputRadius;
        float transPro = VIUISize/radius;

        hoverIn = Math.sqrt(Math.pow(event.getX()-VIX, 2) + Math.pow(event.getY()-VIY, 2)) < S.getInstance().lastInputRadius;

        hoverX = (event.getX() - VIX) * transPro + getWidth()/2;
        hoverY = (event.getY() - VIY )* transPro + getHeight()/2;

        invalidate();
    }


    @Override
    public boolean otherOnTouchEvent(MotionEvent e) {
        Log.v("RingUI", "otherOnTouchEvent");
        float x = e.getX();
        float y = e.getY();

        mCurX =x;
        mCurY =y;

        isMoving = false;

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                isMoving = true;
                double flickLength = Math.sqrt(Math.pow(mDownX-x,2) + Math.pow(mDownY-y,2));
                flick_valid = flickLength > S.getInstance().minFlickRadius;

                if(flick_valid) {
                    Direction dir = Direction.getOpposite(Direction.getDirection(mDownX, mDownY, x, y));
                    AlphabetView leaf = dirToLeaf.get(dir);
                    float progress = Math.min((float) (flickLength / S.getInstance().maxFlickRadius), 1f);
                    if (currLeaf != leaf && currLeaf != null) {
                        currLeaf.recoverToOriginal();
                    }
                    currLeaf = leaf;

                    int pX = (int) (progress * (centerX - leaf.getWidth() / 2) + (1 - progress) * leaf.getOriginalX());
                    int pY = (int) (progress * (centerY - leaf.getHeight() / 2) + (1 - progress) * leaf.getOriginalY());

                    if(flickLength > S.getInstance().validFlickRadius){
                        currLeaf.select();
                    }

                    Log.d("RingUI", String.format("move to x=%d, y=%d", (int) pX, (int) pY));
                    currLeaf.moveToByInstant(pX, pY);
                }else{
                    if(currLeaf != null){
                        currLeaf.recoverToOriginal();
                    }
                    currLeaf = null;
                }

                break;
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                flick_valid = false;

                //Log.d("inputFiledView", "down");
                break;
            case MotionEvent.ACTION_UP:
                if(currLeaf != null){
                    //currLeaf.recoverToOriginal();
                    Log.d("RingUI", String.format("move, action up x=%d, y=%d", (int) centerX, (int) centerY));
                    currLeaf.recoverToOriginal();
                }
                break;
        }



        return false;
    }
}
