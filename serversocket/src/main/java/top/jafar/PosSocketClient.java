package top.jafar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jafar.tang on 2017/8/24.
 */

public class PosSocketClient implements Runnable {

    //socketId
    private String socketId;
    //socket对象
    private Socket socket;
    //输入流
    private BufferedReader reader;
    //输出流
    private Writer writer;
    //当前所在的包
    private String packageScope;
    //类容器
    private static final Map<String, Object> INSTANCE_CONATINER = new HashMap<>();
    //状态
    public static enum POS_SOCKET_STATUS {
        INITED,     // 初始化
        RUNNING,    //运行中
        DEADED,     //结束
    }
    private POS_SOCKET_STATUS status;
    //接受信息回调
    private PosSocketClientCallback posSocketClientCallback;
    private final PosSocketPool posSocketPool;

    public PosSocketClient(Socket socket, PosSocketPool posSocketPool) {
        this.socket = socket;
        this.status = POS_SOCKET_STATUS.INITED;
        this.posSocketPool = posSocketPool;
        this.posSocketClientCallback = getDefaultCallback();
        this.packageScope = this.getClass().getPackage().getName();
        PosSocketLogger.println("初始化PosSocketClient");
        PosSocketLogger.println("当前的包地址: "+packageScope);
        //打开输入输出流
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            PosSocketLogger.println("成功打开输入输出流");
        }catch (Exception e) {
            status = POS_SOCKET_STATUS.DEADED;
            PosSocketLogger.println("打开流时发生了错误: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private PosSocketClientCallback getDefaultCallback() {
        final String pathScope = this.getClass().getPackage().getName();
        return new PosSocketClientCallback() {
            @Override
            public void readLine(String msg, PosSocketClient client) {
                String[] commands = msg.split("\u0003");
                try {
                    String emitterName = commands[0];
                    String methodName = commands[1];
                    String params[] = new String[commands.length-2];
                    for (int i=2; i<commands.length; i++) {
                        params[i-2] = commands[i];
                    }
                    String emitterPath = pathScope+".emitters."+emitterName+"Emitter";
                    Object instance = INSTANCE_CONATINER.get(emitterName);
                    synchronized (PosSocketClient.class) {
                        if(instance == null) {
                            Class clazz = Class.forName(emitterPath);
                            //重新初始化
                            instance = clazz.newInstance();
                            PosSocketLogger.println("初始化"+emitterPath+": "+instance);
                            //是否需要注入
                            Field[] declaredFields = clazz.getDeclaredFields();
                            Field field = null;
                            for (int i=0; i< declaredFields.length; i++) {
                                Field tField = declaredFields[i];
                                Class<?> tFieldType = tField.getType();
                                String tFieldTypeSimpleName = tFieldType.getSimpleName();
                                if(tFieldTypeSimpleName.equals("PosSocketPool")) {
                                    field = tField;
                                    break;
                                }
                            }
                            if(field != null) {
                                field.setAccessible(true);
                                field.set(instance, posSocketPool);
                            }
                            INSTANCE_CONATINER.put(emitterName, instance);
                        }
                    }
                    Method[] methods = instance.getClass().getMethods();
                    Method method = null;
                    Object convertedParams[] = null;
                    Class<?> invokeParameters[] = null;
                    //查找方法
                    for(int i=0; i<methods.length; i++) {
                        Method tMethod = methods[i];
                        if(tMethod.getName().equals(methodName)) {
                            if(true || tMethod.getParameterTypes().length == params.length) {
                                invokeParameters = tMethod.getParameterTypes();
                                convertedParams = new Object[invokeParameters.length];
                                method = tMethod;
                                break;
                            }
                        }
                    }
                    if(method == null) {
                        throw new PosSocketException("没有找到对应的方法: "+methodName);
                    }
                    //是否自动返回结果
                    boolean isNeedAutoReturns = true;
                    // 转换参数
                    for (int i=0; i<convertedParams.length; i++) {
                        Class paramObj = invokeParameters[i];
                        String parameterType = paramObj.getSimpleName();
                        String curParam = i < params.length ? params[i] : null;
                        if("Integer".equals(parameterType) || "int".equals(parameterType)) {
                            convertedParams[i] = Integer.parseInt(curParam);
                        }else if("Float".equals(parameterType) || "float".equals(parameterType)) {
                            convertedParams[i] = Float.parseFloat(curParam);
                        }else if("Boolean".equals(parameterType) || "boolean".equals(parameterType)) {
                            convertedParams[i] = Boolean.parseBoolean(curParam);
                        }else if("Double".equals(parameterType) || "double".equals(parameterType)) {
                            convertedParams[i] = Double.parseDouble(curParam);
                        }else if("String".equals(parameterType)) {
                            convertedParams[i] = curParam;
                        }else if("PosSocketClient".equals(parameterType)) {
                            convertedParams[i] = client;
                            isNeedAutoReturns = false;
                        }else {
                            throw new PosSocketException("不支持转换成该转换: "+parameterType);
                        }
                    }
                    // 执行
                    Object result = method.invoke(instance, convertedParams);
                    if(isNeedAutoReturns) {
                        if(result == null) {
                            client.println("没有返回值!");
                        }else {
                            client.println(result.toString());
                        }
                    }
                }catch (PosSocketException e) {
                    client.println(e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    client.println("发生错误： "+e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 向客户端发送信息
     * @param msg
     * @return
     */
    public boolean println(String msg) {
        boolean b = true;
        try {
            writer.append(msg+"\r\n");
            writer.flush();
            PosSocketLogger.println("成功向+"+socketId+"发送: "+msg);
        } catch (IOException e) {
            b = false;
            PosSocketLogger.println("向+"+socketId+"发送消息时产生了错误: "+e.getMessage());
            e.printStackTrace();
        }
        return b;
    }

    /**
     * 结束socket服务
     */
    public void closeAll() throws IOException {
        if(writer != null) {
            writer.close();
        }
        if(reader != null) {
            reader.close();
        }
    }

    /**
     * 是否已经结束
     * @return
     */
    public boolean isDeaded() {
        return status == POS_SOCKET_STATUS.DEADED;
    }

    @Override
    public void run() {
        String line = null;
        status = POS_SOCKET_STATUS.RUNNING;
        PosSocketLogger.println("开始为"+socketId+"服务...");
        // 接受客户端发来的消息
        do {
            try {
                line = reader.readLine();
                if(line == null) continue;
                PosSocketLogger.println("接受到"+socketId+"传来的消息: "+line);
                if(posSocketClientCallback == null) {
                    //没有对应 callback
                    throw new PosSocketException("没有传入callback!");
                }
                posSocketClientCallback.readLine(line, this);
            }catch (Exception e) {
                PosSocketLogger.println("读取消息时发生了错误: "+e.getMessage());
                status = POS_SOCKET_STATUS.DEADED;
                e.printStackTrace();
                println("发生了错误: "+e.getMessage());
                break;
            }
        }while(line != null);
        status = POS_SOCKET_STATUS.DEADED;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }
}
