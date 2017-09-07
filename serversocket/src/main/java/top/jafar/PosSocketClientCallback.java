package top.jafar;

/**
 * 发送信息回调
 * Created by jafar.tang on 2017/8/24.
 */

public interface PosSocketClientCallback {
    /**
     * 接受客户端发来的消息
     * @param msg
     * @return
     */
    void readLine(String msg, PosSocketClient client);
}
