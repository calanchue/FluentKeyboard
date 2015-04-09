package kr.ac.kaist.jinhwan.fluentkeyboard;

public interface MessageListener{
    public enum Type{
        direction,
        text,
        special
    }
    public void listenMessage(Type type, String m);
}
