package kr.ac.kaist.jinhwan.fluentkeyboard;

public class TestClass {
    private static TestClass ourInstance = new TestClass();

    public static TestClass getInstance() {
        return ourInstance;
    }

    private TestClass() {
    }
}
