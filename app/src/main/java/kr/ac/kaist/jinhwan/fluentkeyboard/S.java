package kr.ac.kaist.jinhwan.fluentkeyboard;


public class S {
    private static S ourInstance = new S();

    public static S getInstance() {
        return ourInstance;
    }

    private S() {
    }

    public float originalAlpha = 0.5f;
    public float selectSize = 2.0f;
    public long recoverDuration = 200;
    public float selectAlpha = 1.0f;
    public float  minFlickRadius = 50;
    public float maxFlickRadius = 100;
    public float validFlickRadius = maxFlickRadius*9/10;
    public boolean fixLastInput = true;


}
