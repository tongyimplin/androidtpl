package jafar.top.maildemo.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import jafar.top.maildemo.R;
import jafar.top.maildemo.fragments.AnotherRightFragment;
import jafar.top.maildemo.greendao.DaoMaster;
import jafar.top.maildemo.greendao.DaoSession;

/**
 * 主类
 * Created by jafar.tang on 2017/5/1.
 */
public abstract class AbstractActivity
        extends AppCompatActivity
        implements View.OnClickListener {
    //=========================LifeCycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //接受上一个界面传过来的参数
        bundle = this.getIntent().getExtras();
        newBundle = new Bundle();
        setDatabase();
        initDao();
    }

    //=========================SQLite部分
    //=========================greendao
    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private static final String DB_NAME = "maildemo-db";
    protected abstract void initDao();
    /**
     * 设置greenDao
     */
    private void setDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        mHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }
    public DaoSession getDaoSession() {
        return mDaoSession;
    }
    public SQLiteDatabase getDb() {
        return db;
    }

    //=========================EventBus部分
    protected void registerEventBus() {
        EventBus.getDefault().register(this);
    }
    protected void unregisterEventBus() {
        EventBus.getDefault().unregister(this);
    }
    protected void postEvent(Object evt) {
        EventBus.getDefault().post(evt);
    }

    //========================注册事件部分
    /**
     * 注册事件
     */
    protected void setOnClickListener(View ...views) {
        for (View view:views) {
            view.setOnClickListener(this);
        }
    }

    //=======================文件操作部分
    /**
     * TODO 读取文件的方法
     */
    protected String readFromFile(String fileName) {
        try {
            FileInputStream inputStream = this.openFileInput(fileName);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            while (inputStream.read(bytes) != -1) {
                arrayOutputStream.write(bytes, 0, bytes.length);
            }
            inputStream.close();
            arrayOutputStream.close();
            String content = new String(arrayOutputStream.toByteArray());
            return content;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 写入文件的方法
     */
    protected  void writeFile(String fileName, String content) {
        this.writeFile( fileName, content, MODE_PRIVATE);
    }

    /**
     * 打开Asserts目录下的文件流
     * @param uri
     * @return
     */
    protected InputStream getFileFromAssets(String uri) {
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open(uri);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    protected void writeFile(String fileName, String content, int mode) {
        if(StringUtils.isEmpty(content)) {
            return ;
        }
        try {
            /* 根据用户提供的文件名，以及文件的应用模式，打开一个输出流.文件不存系统会为你创建一个的，
             * 至于为什么这个地方还有FileNotFoundException抛出，我也比较纳闷。在Context中是这样定义的
             *   public abstract FileOutputStream openFileOutput(String name, int mode)
             *   throws FileNotFoundException;
             * openFileOutput(String name, int mode);
             * 第一个参数，代表文件名称，注意这里的文件名称不能包括任何的/或者/这种分隔符，只能是文件名
             *          该文件会被保存在/data/data/应用名称/files/chenzheng_java.txt
             * 第二个参数，代表文件的操作模式
             *             MODE_PRIVATE 私有（只能创建它的应用访问） 重复写入时会文件覆盖
             *             MODE_APPEND  私有   重复写入时会在文件的末尾进行追加，而不是覆盖掉原来的文件
             *             MODE_WORLD_READABLE 公用  可读
             *             MODE_WORLD_WRITEABLE 公用 可读写
             *  */
            FileOutputStream outputStream = openFileOutput(fileName,
                    mode);
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
            toastText("保存成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据ID查找对象
     */
    protected <T extends View> T $(@IdRes int viewId) {
        return (T)findViewById(viewId);
    }

    //==============================Toast部分
    /**
     * 提示方法
     */
    protected void toastText(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected  void toastTextLong(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    //==============================跳转部分
    /**
     * 上一个界面带过来的参数
     */
    protected Bundle bundle;
    /**
     * 新的bundle
     */
    protected Bundle newBundle;
    /**
     * 不传参数跳转
     * @param clazz
     */
    protected void startActivity(Class<? extends Activity> clazz) {
        startActivity(clazz, null);
    }

    /**
     * 带参数跳转
     * @param clazz
     * @param bundle
     */
    protected void startActivity(Class<? extends Activity> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if(bundle !=null) intent.putExtras(bundle);
        startActivity(intent);
    }
    //==============================Fragment

    /**
     * 替换fragment
     * @param layoutId
     * @param targetFragment
     */
    protected void replaceFragment(@IdRes int layoutId, Fragment targetFragment) {
        replaceFragment(layoutId, targetFragment, false);
    }

    /**
     * 替换fragment是否将当前添加到栈中
     * @param layoutId
     * @param targetFragment
     * @param addToBackStack
     */
    protected void replaceFragment(@IdRes int layoutId, Fragment targetFragment, boolean addToBackStack) {
        AnotherRightFragment anotherRightFragment = new AnotherRightFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(layoutId, targetFragment);
        if(addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

    //==============================日志部分
    protected void D(Object msg){
        Log.d(this.getClass().toString(), msg+"");
    }
    protected void I(Object msg) {
        Log.i(this.getClass().toString(), msg+"");
    }

    protected void W(Object msg) {
        Log.w(this.getClass().toString(), msg+"");
    }

    protected void E(Object msg) {
        Log.e(this.getClass().toString(), msg+"");
    }

    protected void V(Object msg) {
        Log.v(this.getClass().toString(), msg+"");
    }

    //==============================工具部分

    /**
     * 获取ip地址
     * @return
     */
    public static String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;

    }

    protected Handler handler = null;

    /**
     * 发送handler信息
     * @param what
     * @param params
     */
    public void sendHandlerMessage(int what, Map<String, String> params) {
        Message message = new Message();
        message.what = what;
        Bundle bundle = new Bundle();
        Set<Map.Entry<String, String>> entries = params.entrySet();
        for (Map.Entry<String, String> entry: entries) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        message.setData(bundle);
        handler.sendMessage(message);
    }

    protected static Gson GSON = new Gson();
}
