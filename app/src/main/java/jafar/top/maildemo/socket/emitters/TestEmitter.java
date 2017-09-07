package jafar.top.maildemo.socket.emitters;

import top.jafar.PosSocketClient;
import top.jafar.PosSocketPool;

/**
 * Created by jafar.tang on 2017/8/24.
 */

public class TestEmitter {

    private PosSocketPool posSocketPool;

    public void test1(String a, int b) {

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
