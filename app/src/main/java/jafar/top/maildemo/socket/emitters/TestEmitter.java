package jafar.top.maildemo.socket.emitters;

import android.util.Log;
import android.widget.Button;

import top.jafar.PosSocketClient;
import top.jafar.PosSocketLogger;
import top.jafar.PosSocketPool;
import top.jafar.annotation.PosSocketAutowired;

/**
 * Created by jafar.tang on 2017/8/24.
 */

public class TestEmitter {

    // 根据类型注入该对象
    @PosSocketAutowired
    private PosSocketPool posSocketPool;
    // 根据名称注入该对象，建议从项目中注入进来的对象使用第二种方式
    @PosSocketAutowired(value = "saveBtn")
    private Button saveBtn;

    public void test1(String a, int b) {
        PosSocketLogger.println("posSocketPool: "+posSocketPool);
        PosSocketLogger.println("saveBtn: "+saveBtn);
    }

    public String test2(int a, float b) {
        return a+b+"";
    }

    public String test3(boolean b, int a, int c) {
        if(b) {
            return a+c+"";
        }else{
            return a-c+"";
        }
    }

    public String broadcaset(String msg, PosSocketClient mimeClient) {
        posSocketPool.sendAll(msg, mimeClient);
        return "ok";
    }
}
