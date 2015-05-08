package kr.ac.kaist.jinhwan.fluentkeyboard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

import static kr.ac.kaist.jinhwan.fluentkeyboard.Direction.getDirection;

/**
 * Created by Jinhwan on 2015-05-02.
 */
public class InputHistory extends LinkedList<InputHistory.InputData> {
    private static final String TAG = "InputHistory";

    public static class InputData implements Serializable{
        float fromX;
        float fromY;
        float toX;
        float toY;
        float VIX;
        float VIY;
        GestureType type;

        public static enum GestureType{
            AClick,AFlick,Click,Flick,Wild
        }

        long time;
        public InputData(float fromX, float fromY, float toX, float toY, float VIX, float VIY, GestureType type){
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX= toX;
            this.toY = toY;
            this.type = type;
            this.VIX = VIX;
            this.VIY = VIY;
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

    public void drawVIPosHistory(Canvas canvas){
        boolean first = true;
        Paint linePaint = new Paint();
        Paint circlePaint = new Paint();
        Paint last_paint = new Paint();

        last_paint.setColor(Color.MAGENTA);
        circlePaint.setColor(Color.MAGENTA);
        circlePaint.setAlpha(20);

        float[] last= new float[]{0f,0f};
        for(InputData inputData : this){
            float[] position = new float[]{inputData.VIX, inputData.VIY};
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


    public void drawGestureHistory(Canvas canvas) {
        Paint currPaint = new Paint();
        currPaint.setAlpha(20);

        for (InputData inputData : this) {

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


    public static boolean save(String file_path, InputHistory history ){
        try{
            FileOutputStream fos =
                    new FileOutputStream(file_path);
            ObjectOutputStream oos =
                    new ObjectOutputStream(fos);
            oos.writeObject(S.getInstance());
            oos.writeObject(history);
            oos.flush();
            fos.close();
        }
        catch(IOException e) {
            Log.e(TAG, "SAVE ERROR : " + e.toString());
            return false;
        }
        return true;
    }

    public static InputHistory load(String file_path){
        InputHistory loaded;
        S loadedS;
        try
        {
            FileInputStream fis =
                    new FileInputStream(file_path);
            ObjectInputStream ois =
                    new ObjectInputStream(fis);
            loadedS = (S)ois.readObject();
            loaded = (InputHistory)ois.readObject();
            fis.close();
        }
        catch(Throwable e)
        {
            Log.e(TAG,"LOAD ERROR : " + e.toString());
            return null;
        }

        return loaded;
    }
}
