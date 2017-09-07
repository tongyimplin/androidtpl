package jafar.top.maildemo.util;

/**
 * Created by jafar.tang on 2017/8/17.
 */

public interface NetworkUtilCallback {
    static enum DELAY_GRADE {
        // 正常
        NORMAL,
        // 中等
        WARNING,
        // 严重
        DANGER
    }
    void sendMsFromPing(String ms, DELAY_GRADE grade);
}