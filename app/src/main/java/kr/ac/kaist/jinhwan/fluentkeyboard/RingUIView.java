package kr.ac.kaist.jinhwan.fluentkeyboard;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public class RingUIView extends RelativeLayout {
    private String[] _keySet = {"A", "B", "C", "D", "E", "F", "G"};
    private double UI_SIZE = 200;


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        float centerX = getX() + getWidth() / 2;
        float centerY = getY() + getHeight() / 2;

        Log.e("ASDF", String.format("%f, %f, %d, %d", centerX, centerY, getWidth(), getHeight()));
        double radian = 0;
        for (int i = 0; i < _keySet.length; i++) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.alphabet_view, null);
            AlphabetView alphabetView = (AlphabetView) view;
            alphabetView.setText(_keySet[i]);
            this.addView(alphabetView);
            /*float childX = (float)(centerX+UI_SIZE*Math.cos(radian) - alphabetView.getWidth());
            float childY = (float)(centerY+UI_SIZE*Math.sin(radian) - alphabetView.getHeight());*/
            float childX = centerX + 20 * i;
            float childY = centerY + 20 * i;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            params.leftMargin = (int) childX;
            params.topMargin = (int) childY;
            radian += Math.PI / 4;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);



    }

    public RingUIView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
