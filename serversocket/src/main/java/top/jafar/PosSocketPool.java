package top.jafar;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jafar.tang on 2017/8/24.
 */

public class PosSocketPool implements Runnable {

    /**
     * socketclient缓存池
     */
    private static final Map<String, PosSocketClient> POS_SOCKET_POOL = new HashMap<>();
    private ServerSocket serverSocket = null;
    private static final int SERVER_PORT = 8990;

    /**
     * 初始化缓存池
     * @return
     */
    public static PosSocketPool initPool() { synchronized (PosSocketPool.class) {
        PosSocketPool posSocketPool = new PosSocketPool();
        new Thread(posSocketPool).start();
        return posSocketPool;
    } }

    private PosSocketPool() {
        PosSocketLogger.println("初始化PosSocketPool");
        //执行清理socket的任务
        PosSocketClearSchedule.startSchedule(POS_SOCKET_POOL);
    }

    /**
     * 获取单个socket
     * @param socketId
     * @return
     */
    public PosSocketClient getSocketClient(String socketId) {
        PosSocketClient client = POS_SOCKET_POOL.get(socketId);
        PosSocketLogger.println("获取到["+socketId+"]: "+client);
        return client;
    }

    /**
     * 向所有的商户发送信息
     * @param msg
     */
    public void sendAll(final String msg) {
        PosSocketLogger.println("开始群发消息: "+msg);
        iteratePosPool(new PosSocketPoolForeachCallback() {
            @Override
            public void eachOne(String socketId, PosSocketClient client) {
                client.println(msg);
                PosSocketLogger.println("已向"+socketId+"发送!");
            }
        });
    }

    public void sendAll(final String msg, final PosSocketClient mimeClient) {
        PosSocketLogger.println("开始ExceptMe群发消息: "+msg);
        iteratePosPool(new PosSocketPoolForeachCallback() {
            @Override
            public void eachOne(String socketId, PosSocketClient client) {
                if(mimeClient != client) {
                    client.println(msg);
                    PosSocketLogger.println("已向"+socketId+"发送!");
                }
            }
        });
    }

    /**
     * 遍历socket连接池
     * @param callback
     * @throws PosSocketException
     */
    public void iteratePosPool(PosSocketPoolForeachCallback callback) {
        if(callback == null) {
//            throw new PosSocketException("没有callback");
        }
        int length = POS_SOCKET_POOL.size();
        int curIndx = 0;
        for (Map.Entry<String, PosSocketClient> entry: POS_SOCKET_POOL.entrySet()) {
            String socketId = entry.getKey();
            PosSocketClient client = entry.getValue();
            try {
                callback.eachOne(socketId, client);
            }catch (Exception e) {
                e.printStackTrace();
            }
            try {
                callback.eachOne(socketId, client, curIndx);
            }catch (Exception e) {
                e.printStackTrace();
            }
            try {
                callback.eachOne(socketId, client, curIndx, length);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(SERVER_PORT);
            PosSocketLogger.println("服务器监听在: "+SERVER_PORT);
            while(true) {
                //接受到一个新的客户端
                Socket socket = serverSocket.accept();
                PosSocketLogger.println("接受到一个客户端连接...");
                PosSocketClient posSocketClient = new PosSocketClient(socket, this);
                //生成socketid
                String socketId = UUID.randomUUID().toString();
                PosSocketLogger.println("生成socketID: "+socketId);
                posSocketClient.setSocketId(socketId);
                //将socket放入连接池
                POS_SOCKET_POOL.put(socketId, posSocketClient);
                PosSocketLogger.println(socketId+"成功加入连接池");
                //开始服务该socket
                new Thread(posSocketClient).start();
            }
        }catch (Exception e) {
            PosSocketLogger.println("SocketServer启动时发生了错误: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
