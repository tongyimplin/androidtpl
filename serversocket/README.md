

### 开始使用

首先在项目目录下打开terminal，使用gitclone将模块下载下来拷贝进项目,然后在root目录中的settings.gradle加入`include ':serversocket'`，
然后在android的app模块下的build.gradle中的dependency中加入`compile project(':serversocket')`

```
//先设置logger
PosSocketLogger.setLoggerCallback(new PosSockectLoggerCallbackAbs() {
    @Override
    protected void doLog(String msg) {
    Log.i("PosSocket", msg);
    }
});
try {
    //注册emitter需要注入的实例,第一个参数是id不能重复，第二是需要在emitter中注入的对象
    PosSocketBeanUtils.registerBean("saveBtn", saveBtn);
    //设置Emitters的目录,在该目录的emitter目录下
    PosSocketBeanUtils.setConfig("PATH_SCOPE", "jafar.top.maildemo.socket.emitter");
    //配置服务端socket端口
    PosSocketBeanUtils.setConfig("SERVER_PORT", "8993");
} catch (Exception e) {
    e.printStackTrace();
}
final PosSocketPool posSocketPool = PosSocketPool.initPool();
```

### emiiter demo

```
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

```


### java测试socketclient

```
package com.test.serversocket;

import com.km.utils.ErrorUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;


public class TestClient implements Runnable {

    public static void main(String []args) {
        TestClient testClient = new TestClient();
        new Thread(testClient).start();
        testClient.println(StringUtils.join(new Object[]{
                "Test",
                "test1",
                1,
                2
        },"\u0003"));
    }

    //输入流
    private BufferedReader reader;
    //输出流
    private Writer writer;

    public TestClient() {
        //打开输入输出流
        try {
            Socket socket = new Socket("192.168.162.112", 8990);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("成功打开输入输出流");
        }catch (Exception e) {
            System.out.println("打开流时发生了错误: "+ ErrorUtils.getStackTraceString(e));
        }
    }
    public void closeAll() throws IOException {
        if(writer != null) {
            writer.close();
        }
        if(reader != null) {
            reader.close();
        }
    }

    public boolean println(String msg) {
        boolean b = true;
        try {
            writer.append(msg+"\r\n");
            writer.flush();
            System.out.println("成功向+"+"发送: "+msg);
        } catch (IOException e) {
            b = false;
            System.out.println("向+"+"发送消息时产生了错误: "+ErrorUtils.getStackTraceString(e));
            e.printStackTrace();
        }
        return b;
    }

    public void run() {
        String line = null;
        // 接受客户端发来的消息
        do {
            try {
                line = reader.readLine();
                if(line == null) continue;

                System.out.println("来着服务器端的消息: "+line);
            }catch (Exception e) {
                System.out.println("读取消息时发生了错误: "+e.getMessage());
                e.printStackTrace();
                println("发生了错误: "+e.getMessage());
                break;
            }
        }while(line != null);
    }

}

```