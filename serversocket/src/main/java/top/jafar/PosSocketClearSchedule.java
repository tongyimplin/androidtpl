package top.jafar;

import java.io.IOException;
import java.util.Map;

/**
 * pos客户端清理任务
 * Created by jafar.tang on 2017/8/24.
 */

public class PosSocketClearSchedule implements Runnable {
    public static final long NORMAL_RATE = 300; //默认执行频率
    private long rate = NORMAL_RATE; //默认500ms执行一次
    private Map<String, PosSocketClient> socketPool = null;
    private boolean running = true;

    private PosSocketClearSchedule(Map<String, PosSocketClient> socketPool, long rate) {
        this.socketPool = socketPool;
        this.rate = rate;
        PosSocketLogger.println("初始化PosSocketClearSchedule");
    }

    /**
     * 开始执行任务
     * @param socketPool
     */
    public static PosSocketClearSchedule startSchedule(Map<String, PosSocketClient> socketPool) {
        return startSchedule(socketPool, NORMAL_RATE);
    }
    public static PosSocketClearSchedule startSchedule(Map<String, PosSocketClient> socketPool, long rate) {
        PosSocketClearSchedule posSocketClearSchedule = new PosSocketClearSchedule(socketPool, rate);
        new Thread(posSocketClearSchedule).start();
        return posSocketClearSchedule;
    }

    public void stopSchedule() {
        this.running = false;
    }

    @Override
    public void run() {
        while(running) {
            for(Map.Entry<String, PosSocketClient> entry: socketPool.entrySet()) {
                PosSocketClient posSocketClient = entry.getValue();
                //如果该socket已经停止服务，清理掉它
                if(posSocketClient.isDeaded()) {
                    String socketId = entry.getKey();
                    try {
                        posSocketClient.closeAll();
                        PosSocketLogger.println(socketId+"已经死亡,回收所有资源");
                    } catch (IOException e) {
                        PosSocketLogger.println(socketId+"已经死亡,回收过程中发生了错误["+e.getMessage()+"]");
                        e.printStackTrace();
                    }
                    socketPool.remove(socketId);
                    PosSocketLogger.println(socketId+"已被移出socket连接池");
                }
            }
            try {
                Thread.sleep(rate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
