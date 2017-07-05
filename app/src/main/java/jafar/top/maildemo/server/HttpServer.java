package jafar.top.maildemo.server;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;

import jafar.top.maildemo.activities.AbstractActivity;

public class HttpServer
{
    /**
     * WEB_ROOT is the directory where our HTML and other files reside. For
     * this package, WEB_ROOT is the "webroot" directory under the working
     * directory. The working directory is the location in the file system
     * from where the java command was invoked.
     */
    public static final String WEB_ROOT = System.getProperty("user.dir")+File.separator+"webroot";
    // shutdown command
    private static final String SHUTDOWN_COMMAND = "SHUTDOWN";
    // the shutdown command received
    private boolean shutdown = false;
    //Context
    private AbstractActivity abstractActivity;
    private int port = 8080;
    private String hostIP = "127.0.0.1";

    /*public static void main(String[] args)
    {
        HttpServer server = new HttpServer();
        System.out.println("WEB_ROOT: "+WEB_ROOT);
        server.await();
    }*/

    public HttpServer(AbstractActivity abstractActivity) {
        this.abstractActivity = abstractActivity;
        this.port = 8080;
        this.hostIP = abstractActivity.getHostIP();
    }

    public String getHostName() {
        return "http://"+hostIP+":"+port;
    }

    public void shutdownServer() {
        this.shutdown = true;
    }

    private void showMessage(final String msg) {
        abstractActivity.sendHandlerMessage(1, new HashMap<String, String>() {{ put("msg", msg); }});
    }

    public void await()
    {
        ServerSocket serverSocket = null;
        showMessage("start ok");
        try
        {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName(hostIP));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        // Loop waiting for a request
        while (!shutdown)
        {
            Socket socket = null;
            InputStream input = null;
            OutputStream output = null;
            try
            {
                socket = serverSocket.accept();
                input = socket.getInputStream();
                output = socket.getOutputStream();
                // create Request object and parse
                Request request = new Request(input);
                request.parse();
                showMessage(" GET "+request.getUri()+"\r\n"+request.getParameters());
                // create Response object
                Response response = new Response(output, abstractActivity);
                response.setRequest(request);
                //请求静态资源
                response.sendStaticResource();
                // Close the socket
                socket.close();
                // check if the previous URI is a shutdown
                // command
                shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                continue;
            }
        }
    }
}
