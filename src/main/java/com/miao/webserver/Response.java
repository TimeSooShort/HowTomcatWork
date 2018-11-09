package com.miao.webserver;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.*;
import java.util.Locale;

public class Response implements ServletResponse{
    private static final int BUFFER_SIZE = 1024;
    private OutputStream out;
    private PrintWriter writer;
    private Request request;

    public Response(OutputStream out) {
        this.out = out;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    // 静态资源的处理
    public void sendStaticResource() throws IOException{
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream fin = null;
        try {
            File file = new File(Constants.WEB_ROOT, request.getUri());
            if (file.exists()) {
                out.write("HTTP/1.1 200 OK\n".getBytes()); //这里不是\r\n
                out.write("Content-Type: text/html; charset=UTF-8\n\n".getBytes()); //这里不是\r\n
                fin = new FileInputStream(file);
                int readLn;
                while ((readLn = fin.read(buffer, 0, BUFFER_SIZE)) > 0)
                    out.write(buffer, 0, readLn);
            }
            else {
                String errMsg = "HTTP/1.1 404 File Not Found\r\n" + "Content-Type: text/html\r\n"
                        + "Content-Length: 23\r\n" + "\r\n" + "<h1>File Not Found</h1>";
                out.write(errMsg.getBytes());
            }
        } catch (FileNotFoundException e) {
            String errMsg = "HTTP/1.1 404 File Not Found\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: 23\r\n" +
                    "\r\n" +
                    "<h1>File Not Found</h1>";
            out.write(errMsg.getBytes());
        } finally {
            if (fin != null)
                fin.close();
        }
    }

    public String getCharacterEncoding() {
        return null;
    }

    public String getContentType() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    public PrintWriter getWriter() {
        writer = new PrintWriter(out, true);
        return writer;
    }

    public void setCharacterEncoding(String charset) {

    }

    public void setContentLength(int len) {

    }

    public void setContentType(String type) {

    }

    public void setBufferSize(int size) {

    }

    public int getBufferSize() {
        return 0;
    }

    public void flushBuffer() throws IOException {

    }

    public void resetBuffer() {

    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {

    }

    public void setLocale(Locale loc) {

    }

    public Locale getLocale() {
        return null;
    }
}
