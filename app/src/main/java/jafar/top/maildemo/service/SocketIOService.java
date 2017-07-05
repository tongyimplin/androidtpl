package jafar.top.maildemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.FileDescriptor;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by jafar.tang on 2017/5/26.
 */

public class SocketIOService extends Service {
    private static final String TAG = SocketIOService.class.getName();

    private static final String COMMON_TAG = "CommonTag";
    private Socket socket = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(COMMON_TAG, "onBind");
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            opts.query = "auth_token=android";
            socket = IO.socket("http://192.168.162.46:8970", opts);
            socket.on(Socket.EVENT_CONNECT, EMIIT_LISTENERS.CONNECT)
                    .on("event", EMIIT_LISTENERS.EVENT)
                    .on(Socket.EVENT_DISCONNECT, EMIIT_LISTENERS.DISCONNECT)
                    .on("ping1", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.d(COMMON_TAG, "接受到参数: "+args[0]);
                            Ack ack = (Ack) args[args.length - 1];
                            ack.call(null, "");
                        }
                    });
            socket.connect();

        }catch (Exception e) {

        }
        return new SocketBinder();
    }

    static class EMIIT_LISTENERS {
        static Emitter.Listener CONNECT = new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        };
        static Emitter.Listener DISCONNECT = new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        };
        static Emitter.Listener EVENT = new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        };
    }

    public class SocketBinder extends Binder {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(COMMON_TAG, "创建服务");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(COMMON_TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.close();
        socket = null;
        Log.i(COMMON_TAG, "结束服务");
    }
}
