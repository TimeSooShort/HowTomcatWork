package com.miao.webserver.SecondEdition.connector;

import com.miao.webserver.firstEdition.Request;
import com.miao.webserver.firstEdition.Response;
import com.miao.webserver.util.Constants;

import java.io.*;

public class StaticResourceProcessor {

    public void process(HttpRequest request, HttpResponse response) throws IOException {
        // 静态资源的处理
        OutputStream output = response.getStream();
        byte[] buffer = new byte[1024];
        FileInputStream fin = null;
        try {
            File file = new File(Constants.WEB_ROOT, request.getRequestURI());
            if (file.exists()) {
                output.write("HTTP/1.1 200 OK\n".getBytes());
                output.write("Content-Type: text/html; charset=UTF-8\n\n".getBytes());
                fin = new FileInputStream(file);
                int readLn;
                while ((readLn = fin.read(buffer, 0, 1024)) > 0)
                    output.write(buffer, 0, readLn);
            }
            else {
                String errMsg = "HTTP/1.1 404 File Not Found\r\n" + "Content-Type: text/html\r\n"
                        + "Content-Length: 23\r\n" + "\r\n" + "<h1>File Not Found</h1>";
                output.write(errMsg.getBytes());
            }
        } catch (FileNotFoundException e) {
            String errMsg = "HTTP/1.1 404 File Not Found\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: 23\r\n" +
                    "\r\n" +
                    "<h1>File Not Found</h1>";
            output.write(errMsg.getBytes());
        } finally {
            if (fin != null)
                fin.close();
        }
    }
}
