package jafar.top.maildemo.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;


import jafar.top.maildemo.R;
import jafar.top.maildemo.entities.User;
import jafar.top.maildemo.events.MessageEvent;
import jafar.top.maildemo.greendao.DaoSession;
import jafar.top.maildemo.greendao.TeacherDao;
import jafar.top.maildemo.greendao.UserDao;
import jafar.top.maildemo.server.HttpServer;
import jafar.top.maildemo.service.SocketIOService;


public class MainActivity extends AbstractActivity {

    private static final String COMMON_TAG = "CommonTag";

    private Button saveBtn;
    private Button fetchBtn;
    private Button triggerBtn;

    private ImageView imageView;
    private TextView editText;
    private TextView pingText;

    private UserDao userDao;
    private TeacherDao teacherDao;
    private StringBuffer consoleStr = new StringBuffer();
    private boolean isServerStarted = false;
    private HttpServer server = new HttpServer(MainActivity.this);;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveBtn = $(R.id.save_btn);
        fetchBtn = $(R.id.fetch_btn);
        triggerBtn = $(R.id.trigger_evt);
        editText = $(R.id.edit_text);
        imageView = $(R.id.imageView);
        pingText = $(R.id.ping_txt);

//        editText.setEnabled(false);
        editText.setMovementMethod(ScrollingMovementMethod.getInstance());
        setOnClickListener(saveBtn, fetchBtn, triggerBtn);
        //自动显示最新的文本
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int maxLine = 28;
                if(editText.getLineCount() > maxLine) {
                    int totalHeight = (editText.getLineCount() - (maxLine-1)) * editText.getLineHeight();
                    editText.scrollTo(0, totalHeight);
                }
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        showMsg(msg.getData().getString("msg"));
                        break;
                    case 2:
                        String url = msg.getData().getString("url");
                        createQRImage(url);
                        break;
                }
            }
        };
        registerEventBus();
    }
    private String fileName ="test.log";
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_btn:
//                saveHandler();
//                disconnectToSocket();
                pingText.setText("300ms");
                break;
            case R.id.fetch_btn:
//                fetchBtn();
                sendPing();
                break;
            case R.id.trigger_evt:
//                triggerEvent();
                connectToSocket();
                break;
        }
    }
    private boolean isBind = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(COMMON_TAG, "onServiceConnted");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(COMMON_TAG, "onServiceDisconnected");
        }
    };
    /**
     * 连接到socket.io服务器
     */
    private void connectToSocket() {
        showMsg("准备连接到服务器");
        bindService(new Intent(MainActivity.this, SocketIOService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 端口socket.io服务器连接
     */
    private void disconnectToSocket() {
        showMsg("断开服务器连接");
        unbindService(serviceConnection);
    }

    //发送一个测试信息到服务器
    private void sendPing() {
        showMsg("发送测试信息");
    }

    private void showMsg(String msg) {
        consoleStr
                .append("[")
                .append(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()))
                .append("] ")
                .append(msg).append("\r\n");
        editText.setText(consoleStr.toString());
    }

    private void triggerEvent() {
//        newBundle.putString("name", "Jboss");
//        startActivity(NewsActivity.class, newBundle);
//        postEvent(new MessageEvent.TestEvent());
//        Message message = new Message();
//        message.what = 2;
//        handler.sendMessage(message);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url = server.getHostName();
                sendHandlerMessage(1, new HashMap<String, String>() {{
                    put("msg", "初始化成功, 服务器监听在:"+url);
                }});
                sendHandlerMessage(2, new HashMap<String, String>() {{ put("url", url); }});
                server.await();
            }
        }).start();
        triggerBtn.setEnabled(false);
    }

    int QR_WIDTH = 200;
    int QR_HEIGHT = 100;

    /**
     * 创建二维码
     * @param url
     */
    public void createQRImage(String url){
        try
        {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1)
            {
                return;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < QR_HEIGHT; y++)
            {
                for (int x = 0; x < QR_WIDTH; x++)
                {
                    if (bitMatrix.get(x, y))
                    {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    }
                    else
                    {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            //显示到一个ImageView上面
            imageView.setImageBitmap(bitmap);
        }
        catch (WriterException e)
        {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent.TestEvent event) {
    }

    private void fetchBtn() {
        showMsg("获取到本机的IP: " +getHostIP());
    }

    private void saveHandler() {
        //关闭服务器
        server.shutdownServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterEventBus();
    }

    @Override
    protected void initDao() {
        DaoSession daoSession = getDaoSession();
        userDao = daoSession.getUserDao();
        teacherDao = daoSession.getTeacherDao();
    }



}

