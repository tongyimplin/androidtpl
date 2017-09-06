package jafar.top.maildemo.socket;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jafar.tang on 2017/8/24.
 */

public abstract class PosSockectLoggerCallback {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private void doLog(String msg) {
        System.out.println(msg);
    }

    /**
     * 打印带日期的信息
     * @param msg
     */
    public void println(String msg) {
        doLog("["+dateFormat.format(new Date())+"]"+msg);
    }

}
