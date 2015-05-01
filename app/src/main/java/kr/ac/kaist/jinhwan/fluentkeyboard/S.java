package kr.ac.kaist.jinhwan.fluentkeyboard;


import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class S implements Serializable{
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
    public float minFlickRadius = 50;
    public float maxFlickRadius = 100;
    public float validFlickRadius = maxFlickRadius*9/10;
    //public boolean fixLastInput = true;
    public float ringScale=1f;
    public int ringOffX=0;
    public int ringOffY=0;
    public float lastInputRadius = 100f;
    public float longPressInterval = 500;
    public InputOption inputOption = InputOption.Fixed;
    public int adaptHistorySize = 10;


    public static void makeDefaultSaveDirectory(){
        makeDirectory(DIR_PATH);
    }
    public static String getDefaultSaveDirPath(){
        return DIR_PATH;
    }

    public boolean save(String file_path){
/*        File file = new File(file_path);
        Log.i( TAG , "!file.exists" );
        try {
            isSuccess = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally{
            Log.i(TAG, "파일생성 여부 = " + isSuccess);
        }*/

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

    private static File makeDirectory(String dir_path){
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
