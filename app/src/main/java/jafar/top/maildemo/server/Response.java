package jafar.top.maildemo.server;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jafar.top.maildemo.activities.AbstractActivity;

/*
  HTTP Response = Status-Line
    *(( general-header | response-header | entity-header ) CRLF)
    CRLF
    [ message-body ]
    Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
*/

public class Response {

    private static final int BUFFER_SIZE = 1024;
    Request request;
    OutputStream output;
    private AbstractActivity abstractActivity;

    public Response(OutputStream output, AbstractActivity abstractActivity) {
        this.output = output;
        this.abstractActivity = abstractActivity;
    }

    private void showMessage(final String msg) {
        abstractActivity.sendHandlerMessage(1, new HashMap<String, String>() {{ put("msg", msg); }});
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    private void writeHeaders(OutputStream output, long len) throws IOException {
        String headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html;charset=UTF-8\r\n" +
                "Content-Length: " + len +
                "\r\n\r\n";
        output.write(headers.getBytes());
    }

    private int getStrLength(String str) {
        return str.getBytes().length;
    }

    public void sendStaticResource() throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        InputStream fis = null;
        try {
//            File file = new File(HttpServer.WEB_ROOT, request.getUri());
            if(request.getUri().matches("[^\\.]+\\.form$")) {
                String resultStr = "<p>您请求的地址是: "+request.getUri()+"</p>";
                resultStr += "<ul>您传递的值是:";
                Map<String, String> parameters = request.getParameters();
                Set<Map.Entry<String, String>> entries = parameters.entrySet();
                for (Map.Entry<String, String> entry: entries) {
                    resultStr += "<li>"+entry.getKey()+" : "+entry.getValue()+"</li>";
                }
                resultStr+="</ul>";
                showMessage("返回值: \r\n"+resultStr);
                writeHeaders(output, getStrLength(resultStr));
                output.write(resultStr.getBytes());
            }else {
                try {
                    InputStream open = abstractActivity.getResources().getAssets().open("html/" +request.getUri());
                    fis = open;
                    writeHeaders(output, fis.available());
                    int ch = fis.read(bytes, 0, BUFFER_SIZE);
                    System.out.println(new String(bytes, "utf-8"));
                    while (ch!=-1) {
                        output.write(bytes, 0, ch);
                        ch = fis.read(bytes, 0, BUFFER_SIZE);
                    }
                } catch (FileNotFoundException e) {
                    // file not found
                    String notFoundStr = "<h1>您访问的文件没有找到</h1><p>Powered by <a href=\"http://www.yingqianpos.com\">赢钱云POS</a></p>";
                    String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
                            "Content-Type: text/html;charset=utf-8\r\n" +
                            "Content-Length: "+ getStrLength(notFoundStr) +
                            "\r\n\r\n" +
                            notFoundStr;
                    output.write(errorMessage.getBytes());
                }
            }
        }
        catch (Exception e) {
            // thrown if cannot instantiate a File object
            System.out.println(e.toString() );
        }
        finally {
            if (fis!=null)
                fis.close();
        }
    }
}
