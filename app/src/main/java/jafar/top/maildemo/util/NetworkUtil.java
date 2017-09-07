package jafar.top.maildemo.util;

import android.app.Activity;
import android.util.Log;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NetworkUtil implements Runnable {
    private NetworkUtilCallback callback;
    public static final int TIME_OUT_VALUE = 500;
    /**
     * 连接超时
     */
    public static final int TIME_OUT = 3;
    /**
     * 超时计数器
     */
    private static int timeOutCount = 0;

    /**
     * 延时权重数组
     */
    private int timeDelayArray[] = new int[20];
    /**
     * 上一次的权重延时
     */
    private int lastAvgMs = 0;
    private boolean isFirst = true;

    public NetworkUtil( NetworkUtilCallback callback) {
        this.callback = callback;
        for(int i=0; i< timeDelayArray.length; i++) {
            timeDelayArray[i] = TIME_OUT_VALUE;
        }
    }

    private String getMsFromPing(String sourceStr) {
        if(StringUtils.isBlank(sourceStr)) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?\\s?ms");
        Matcher matcher = pattern.matcher(sourceStr);
        if(matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public void startWork() {
//        String command = "ping -t www.yingqianpos.com";
        String command = "ping kmbk.xiaozhuzhu.top";
        Runtime runtime = Runtime.getRuntime();
        BufferedReader reader = null;
        try {
            Process process = runtime.exec(command);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
            String line = null;
            while((line = reader.readLine()) != null) {
                String msFromPing = getMsFromPing(line);
                Log.d("NetworkUtil", line);
                if(msFromPing != null) {
                    timeOutCount = 0;
                    appendDelayMs(msFromPing);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 追加延时到权重数组
     * @param ms
     */
    private void appendDelayMs(String ms) {
        int delayMs = TIME_OUT_VALUE;
        try{
            float delayMsFloat = Float.parseFloat(ms.replace("ms", "").trim());
            delayMs = (int) delayMsFloat;
        }catch (Exception e) {
            e.printStackTrace();
        }
        if(isFirst) {
            for(int i=0; i< timeDelayArray.length; i++) {
                timeDelayArray[i] = delayMs;
            }
            isFirst = false;
        }else {
            timeDelayArray = ArrayUtils.remove(timeDelayArray, 0);
            timeDelayArray = ArrayUtils.add(timeDelayArray, delayMs);
        }

        int avgMs = getAvgDelayMs(timeDelayArray);
        NetworkUtilCallback.DELAY_GRADE delayGrade = getDelayGrade(avgMs);
        callback.sendMsFromPing(avgMs+"ms", delayGrade);
    }

    /**
     * 获取延迟等级
     * @param avgMs
     * @return
     */
    private NetworkUtilCallback.DELAY_GRADE getDelayGrade(int avgMs) {
        if(avgMs < 110) {
            return NetworkUtilCallback.DELAY_GRADE.NORMAL;
        }else if(avgMs < 200) {
            return NetworkUtilCallback.DELAY_GRADE.WARNING;
        }else {
            return NetworkUtilCallback.DELAY_GRADE.DANGER;
        }
    }

    /**
     * 获取权重平均值
     * @param delayArray
     * @return
     */
    private int getAvgDelayMs(int[] delayArray) {
        int totalDelayMs = 0;
        StringBuilder msBuilder = new StringBuilder("[");
        for (int i=0; i< delayArray.length; i++) {
            int time = delayArray[i];
            msBuilder.append(time);
            if( i < delayArray.length-1 ) {
                msBuilder.append(", ");
            }
            totalDelayMs += time;
        }
        msBuilder.append("]");
        System.out.println(msBuilder.toString());
        int avgMs = totalDelayMs/delayArray.length;
        return avgMs;
    }

    @Override
    public void run() {
        while(true) {
            timeOutCount ++;
            if(timeOutCount >= TIME_OUT) {
                //超时了
                appendDelayMs(TIME_OUT_VALUE+"");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

