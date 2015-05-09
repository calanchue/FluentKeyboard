package kr.ac.kaist.jinhwan.fluentkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Jinhwan on 2015-05-09.
 */
public class FeedBackView extends View {
    Paint paint;
    float radius;
    public FeedBackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.rgb(0,102,0));
        float strokeSize= S.getInstance().dpToPx(5);
        paint.setStrokeWidth(strokeSize);
        paint.setStyle(Paint.Style.STROKE);
        float smaller = getWidth() > getHeight() ? getWidth():getHeight();
        radius = smaller- strokeSize/2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth()/2, getHeight()/2,radius, paint);
        super.onDraw(canvas);
    }
}
