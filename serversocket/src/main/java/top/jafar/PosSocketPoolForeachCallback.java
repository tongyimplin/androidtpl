package top.jafar;

/**
 * Created by jafar.tang on 2017/8/24.
 */

public abstract class PosSocketPoolForeachCallback {
    /**
     * 单独每个
     * @param socketId
     * @param client
     */
    void eachOne(String socketId, PosSocketClient client) {}

    /**
     *
     * @param socketId
     * @param client
     * @param curIndex  当前的index
     */
    void eachOne(String socketId, PosSocketClient client, int curIndex) {}

    /**
     *
     * @param socketId
     * @param client
     * @param curIndex
     * @param length    长度
     */
    void eachOne(String socketId, PosSocketClient client, int curIndex, int length) {}
}
