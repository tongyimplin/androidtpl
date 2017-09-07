package top.jafar;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jafar.tang on 2017/8/24.
 */

public abstract class PosSockectLoggerCallbackAbs implements PosSocketLoggerCallback {
//    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String dateFormatStr = "yyyy-MM-dd HH:mm:ss.SSS";


    private boolean needFormatDate = false;

    protected abstract void doLog(String msg);

    /**
     * 打印带日期的信息
     * @param msg
     */
    public void println(String msg) {
        if(needFormatDate) {
            doLog("["+ DateFormatUtils.format(new Date(), dateFormatStr)+"] "+msg);
        }else{
            doLog(msg);
        }
    }

}
