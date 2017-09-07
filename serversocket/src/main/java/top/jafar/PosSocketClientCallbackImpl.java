package top.jafar;

/**
 * Created by jafar.tang on 2017/8/24.
 */

public class PosSocketClientCallbackImpl implements PosSocketClientCallback {

    private PosSocketPool posSocketPool;
    private PosSocketClient posSocketClient;
    private String curEmitter;
    private String curMethod;
    private Object[] curParams;

    public PosSocketClientCallbackImpl(PosSocketPool posSocketPool, PosSocketClient posSocketClient) {
        this.posSocketPool = posSocketPool;
        this.posSocketClient = posSocketClient;
    }

    @Override
    public void readLine(String msg, PosSocketClient client) {

    }
}
