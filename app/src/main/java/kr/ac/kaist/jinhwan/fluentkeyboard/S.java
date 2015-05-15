package kr.ac.kaist.jinhwan.fluentkeyboard;


import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class S implements Serializable{

    private static DisplayMetrics metrics;
    private float dpDensity;

    public void setDisplayMetrics(DisplayMetrics metrics){
        this.metrics = metrics;
        dpDensity = metrics.density;
    }

    public float pxToMm(float pix){
        return  pix / metrics.xdpi * 25.4f;
    }

    public float mmToPx(float mm){
        return mm * metrics.xdpi * (1.0f/25.4f);
    }

    public float pxToDp(float pix){
        return  pix / dpDensity;
    }

    public float dpToPx(float mm){
        return mm * dpDensity;
    }

    public float getMinFlickRadius() {
        //return mmToPx(minFlickRadius);
        return minFlickRadius;
    }

    public void setMinFlickRadius(float minFlickRadius) {
        //this.minFlickRadius = pxToMm(minFlickRadius);
        this.minFlickRadius = minFlickRadius;
    }

    public float getMaxFlickRadius() {
        //return mmToPx(maxFlickRadius);
        return maxFlickRadius;
    }

    public void setMaxFlickRadius(float maxFlickRadius) {
        //this.maxFlickRadius = pxToMm(maxFlickRadius);
        this.maxFlickRadius =maxFlickRadius;
    }

    public float getValidFlickRadius() {
        //return mmToPx(validFlickRadius);
        return validFlickRadius;
    }

    public void setValidFlickRadius(float validFlickRadius) {
        //this.validFlickRadius = pxToMm(validFlickRadius);
        this.validFlickRadius = validFlickRadius;
    }

    public float getLastInputRadius() {
        //return dpToPx(lastInputRadius);
        return lastInputRadius;
    }

    public void setLastInputRadius(float lastInputRadius) {
        //this.lastInputRadius = pxToDp(lastInputRadius);
        this.lastInputRadius = lastInputRadius;
    }

    public static enum InputOption {
        Fixed(R.id.inputFixedRB),Last(R.id.inputLastRB),
        AdaptAll(R.id.inputAdaptAllRB),AdaptConsonant(R.id.inputAdaptConsonantRB),
        AdaptVowel(R.id.inputAdaptVowelRB);

        int id;
        InputOption(int id){
            this.id=id;
        }
        public int getId(){
            return  id;
        }

        public static InputOption getFromId(int id){
            for(InputOption option : InputOption.values()){
                if(option.getId() == id){
                    return option;
                }
            }
            return Fixed;
        }
    }

    private static S ourInstance = new S();
    private static String TAG = "Setting";
    private static String DIR_PATH = Environment.
            getExternalStorageDirectory()+"/fluentKbdSetting/";

    public static S getInstance() {
        return ourInstance;
    }

    private S() {
    }

    public float originalAlpha = 0.5f;
    public float selectSize = 2.0f;
    public long recoverDuration = 500;
    public float selectAlpha = 1.0f;
    private float minFlickRadius = 50;
    private float maxFlickRadius = 100;
    private float validFlickRadius = maxFlickRadius *9/10;
    //public boolean fixLastInput = true;
    public float ringScale=1f;
    public int ringOffX=0;
    public int ringOffY=0;
    private float lastInputRadius = 100;
    public float longPressInterval = 500;
    public InputOption inputOption = InputOption.Fixed;
    public int adaptHistorySize = 10;
    public boolean hoverTrack = false;
    public boolean inDirFromStartPos = true; //false = Direction form movement Direction
    public float ringUISize = 50;
    public float bent2MinFlickRadius = 20;


    public static void makeDefaultSaveDirectory(){
        makeDirectory(DIR_PATH);
    }
    public static String getDefaultSaveDirPath(){
        return DIR_PATH;
    }

    public boolean save(String file_path){
        try{
            FileOutputStream fos =
                    new FileOutputStream(file_path);
            ObjectOutputStream oos =
                    new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.flush();
            fos.close();
        }
        catch(IOException e) {
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    public static boolean load(String file_path){
        S loadedS;
        try
        {
            FileInputStream fis =
                    new FileInputStream(file_path);
            ObjectInputStream ois =
                    new ObjectInputStream(fis);
            loadedS = (S)ois.readObject();
            fis.close();
        }
        catch(Throwable e)
        {
            Log.e(TAG, e.toString());
            return false;
        }

        ourInstance = loadedS;

        return true;
    }

    public static File makeDirectory(String dir_path){
        File dir = new File(dir_path);
        if (!dir.exists())
        {
            dir.mkdirs();
            Log.i( TAG , "!dir.exists" );
        }else{
            Log.i( TAG , "dir.exists" );
        }

        return dir;
    }



}
