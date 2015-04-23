package kr.ac.kaist.jinhwan.fluentkeyboard;


import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

public class AlphabetView extends TextView {

    Paint paint = new Paint();

    private int originalX, originalY;

    private boolean isSelected = false;


    public AlphabetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(6f);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        setAlpha(S.getInstance().originalAlpha);
    }



    public void setOriginalPosition(int x, int y){
        originalX = x;
        originalY= y ;
    }

    public int getOriginalX(){
        return originalX;
    }
    public int getOriginalY(){
        return originalY;
    }

    public void recoverToOriginal(){
        Log.d("RecoverToOrigin", String.format("x=%d, y=%d", originalX, originalY));
        this.animate().setInterpolator(new OvershootInterpolator()).x(originalX).y(originalY ).
                scaleX(1.0f).scaleY(1.0f).
                alpha(S.getInstance().originalAlpha).setDuration(S.getInstance().recoverDuration).start();
        isSelected = false;
    }

    public void moveToPositionByAnimation(int x, int y){
        this.animate().x(x ).y(y).start();
    }

    public void confirmMove(){
        AnimatorSet move = new AnimatorSet();



    }

    public void select(){
        if(isSelected){
        }else {
            isSelected = true;
            this.animate().setInterpolator(new OvershootInterpolator()).
                    scaleX(S.getInstance().selectSize).scaleY(S.getInstance().selectSize)
                    .alpha(S.getInstance().selectAlpha).setDuration(200);
        }
    }


    public void moveToByInstant(int x, int y){
        this.animate().x(x ).y(y ).setDuration(0).start();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("onDraw", String.format("%f %f : %f %f", mDownX,mDownY,mCurX, mCurY));

        canvas.drawCircle(getWidth()/2, getHeight()/2 , 10, paint);
    }
}


