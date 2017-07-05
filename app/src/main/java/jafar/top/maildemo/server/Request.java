package jafar.top.maildemo.server;


import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private InputStream input;
    private String uri;
    private String queryStr = "";
    private Map<String, String> parameters = new HashMap<>();

    public Request(InputStream input) {
        this.input = input;
    }

    public void parse() {
        // Read a set of characters from the socket
        StringBuffer request = new StringBuffer(2048);
        int i;
        byte[] buffer = new byte[2048];
        try {
            i = input.read(buffer);
        }
        catch (IOException e) {
            e.printStackTrace();
            i = -1;
        }
        for (int j=0; j<i; j++) {
            request.append((char) buffer[j]);
        }
        System.out.print(request.toString());
        uri = parseUri(request.toString());
    }
    public String getParameter(String key) {
        return parameters.get(key);
    }
    public Map<String, String> getParameters() {
        return parameters;
    }
    /**
     * 当前请求是否为静态资源
     * @return
     */
    public boolean isStaticFile() {
        return uri.matches("[^\\.]+\\.(html|css|js|jpg|png)$");
    }
    private String parseUri(String requestString) {
        int index1, index2;
        index1 = requestString.indexOf(' ');
        if (index1 != -1) {
            index2 = requestString.indexOf(' ', index1 + 1);
            if (index2 > index1) {
                String url = requestString.substring(index1 + 2, index2);
                if(url.indexOf("?") != -1) {
                    String[] split = url.split("\\?");
                    uri = split[0];
                    queryStr = split[1];
                    String[] split1 = queryStr.split("&");
                    for (int i = 0; i < split1.length; i++) {
                        String[] split2 = split1[i].split("=");
                        parameters.put(split2[0], split2[1]);
                    }
                }else {
                    uri = url;
                }
                return uri;
            }

        }
        return "";
    }

    public String getUri() {
        return uri;
    }

}