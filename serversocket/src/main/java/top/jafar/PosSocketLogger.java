package top.jafar;

/**
 * Created by jafar.tang on 2017/8/24.
 */

public class PosSocketLogger {
    public static PosSocketLoggerCallback CALLBACK = null;

    static {
        CALLBACK = new PosSockectLoggerCallbackAbs() {
            @Override
            protected void doLog(String msg) {
                System.out.println(msg);
            }
        };
    }

    public static void setLoggerCallback(PosSocketLoggerCallback callback) {
        CALLBACK = callback;
    }

    /**
     * 写入
     * @param line
     */
    public static void println(String line) {
        CALLBACK.println(line);
    }
}
