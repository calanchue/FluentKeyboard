package kr.ac.kaist.jinhwan.fluentkeyboard;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.LinkedList;

public class RingUIView extends RelativeLayout {
    private String[] _keySet = {"A", "B", "C", "D", "E", "F", "G"};
    private double UI_SIZE = 100;

    float centerX ;// = getX() + getWidth() / 2;
    float centerY ;//= getY() + getHeight() / 2;

    Paint paint = new Paint();
    LinkedList<AlphabetView> ringLeafList = new LinkedList<>();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        double radian = 0;

        /*float centerX = getWidth()/2;
        float centerY = getHeight()/2;*/

        centerX = getX() + getWidth() / 2;
        centerY = getY() + getHeight() / 2;

        UI_SIZE = 0.6 * getWidth()/2;


        Log.e("ASDF", String.format("%f, %f, %d, %d", getX(), getY(), getWidth(), getHeight()));
        /*AlphabetView testview = ringLeafList.get(0);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) testview.getLayoutParams();
        params.leftMargin = (int) (centerX);
        params.topMargin = (int) (centerY);*/



        for (int i = 0; i < _keySet.length; i++) {
            AlphabetView alphabetView = ringLeafList.get(i);
            //float childX = (float)(centerX+UI_SIZE*Math.cos(radian) - alphabetView.getWidth());
            //float childY = (float)(centerY+UI_SIZE*Math.sin(radian) - alphabetView.getHeight());
            float childX = (float)(getWidth()/2 + UI_SIZE*Math.cos(radian) - alphabetView.getWidth()/2);
            float childY = (float)(getHeight()/2 + UI_SIZE*Math.sin(radian) - alphabetView.getHeight()/2);

            //float childX = centerX + 10 * i;
            //float childY = centerY + 10 * i;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) alphabetView.getLayoutParams();
            //params.addRule(CENTER_IN_PARENT);
            //params.addRule(ALIGN_PARENT_LEFT);
            //params.leftMargin = (int) childX;
            //params.topMargin = (int) childY;

            params.setMargins((int)childX, (int)childY, 0, 0);

            //alphabetView.setLayoutParams(params);

            Log.d("debug", ""+i);
            Log.d("debug",String.format("alphabetView position %f %f" , alphabetView.getX(), alphabetView.getY())  );

            radian += Math.PI / 4;
        }

        //invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);



    }

    public RingUIView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
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
        }


    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("onDraw", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));

            canvas.drawCircle(getWidth()/2, getHeight()/2 , 10, paint);

    }
}
