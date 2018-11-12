package com.miao.webserver.SecondEdition.connector;

import com.miao.webserver.util.Constants;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collection;
import java.util.Locale;

public class HttpResponse implements HttpServletResponse {

    private static final int BUFFER_SIZE = 1024; // 默认buffer大小
    private HttpRequest request;
    private OutputStream output;
    private PrintWriter writer;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private String encoding;

    private int bufferCount; // buffer数组里字节个数

    private int contentCount; //被写入到输出流中的数据总大小

    public HttpResponse(OutputStream output) {
        this.output = output;
    }


    // ===============================================
    //获取HttpRequest
    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    // 将数据发送到输出流，关闭输出流
    public void finishResponse() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

    // 将字节b写入到输出流
    public void write(int b) throws IOException {
        if (bufferCount >= BUFFER_SIZE)
            flushBuffer();
        buffer[bufferCount++] = (byte)b;
        contentCount++;
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * 利用 递归 + flushBuffer方法 来将数据写入到输出流中
     * @param b 数据
     * @param off 开始
     * @param len 总长
     * @throws IOException
     */
    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) return;
        if (len <= BUFFER_SIZE - bufferCount) {
            System.arraycopy(b, off, buffer, bufferCount, len);
            bufferCount += len;
            contentCount += len;
            return;
        }

        // 递归过程中每当buffer满了，就会将该BUFFER_SIZE大小的数据chunk，
        // 一次性写入到输出流中
        flushBuffer();
        int loopCount = len / BUFFER_SIZE;
        int loopContent = loopCount*BUFFER_SIZE;
        int restLen = len - loopContent;
        for (int i = 0; i < loopCount; i++) {
            write(b, off+i*BUFFER_SIZE, BUFFER_SIZE);
        }
        // 将剩下部分写入到buffer数组中
        if (restLen > 0) {
            write(b, off+loopContent, restLen);
        }
    }

    public OutputStream getStream() {
        return this.output;
    }





    // ===========================================

    public void flushBuffer() throws IOException {
        if (bufferCount > 0) {
            try {
                output.write(buffer, 0, bufferCount);
            }
            finally {
                bufferCount = 0;
            }
        }
    }

    // 下面实现的功能可以用这行代码来替代
    // new ResponseWrite(new BufferedWriter(new OutputStreamWriter(new ResponseStream(this), getCharacterEncoding())));
    public PrintWriter getWriter() throws IOException {
        if (writer != null) return writer;
        ResponseStream stream = new ResponseStream(this);
        stream.setCommit(false);// 通过这一步，实现了只有当buffer数组满时才会刷新的功能
        OutputStreamWriter osr = new OutputStreamWriter(stream, getCharacterEncoding());
        writer = new ResponseWrite(osr);
        return writer;
    }

    public void addCookie(Cookie cookie) {

    }

    public boolean containsHeader(String name) {
        return false;
    }

    public String encodeURL(String url) {
        return null;
    }

    public String encodeRedirectURL(String url) {
        return null;
    }

    public String encodeUrl(String url) {
        return null;
    }

    public String encodeRedirectUrl(String url) {
        return null;
    }

    public void sendError(int sc, String msg) throws IOException {

    }

    public void sendError(int sc) throws IOException {

    }

    public void sendRedirect(String location) throws IOException {

    }

    public void setDateHeader(String name, long date) {

    }

    public void addDateHeader(String name, long date) {

    }

    public void setHeader(String name, String value) {

    }

    public void addHeader(String name, String value) {

    }

    public void setIntHeader(String name, int value) {

    }

    public void addIntHeader(String name, int value) {

    }

    public void setStatus(int sc) {

    }

    public void setStatus(int sc, String sm) {

    }

    public int getStatus() {
        return 0;
    }

    public String getHeader(String name) {
        return null;
    }

    public Collection<String> getHeaders(String name) {
        return null;
    }

    public Collection<String> getHeaderNames() {
        return null;
    }

    public String getCharacterEncoding() {
        if (encoding == null) return "ISO-8859-1";
        else return encoding;
    }

    public String getContentType() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return null;
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
