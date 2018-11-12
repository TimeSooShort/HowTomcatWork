package com.miao.webserver.SecondEdition.connector;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ResponseStream extends ServletOutputStream {

    // 输出流
    protected OutputStream outputStream;

    // 已被写入的数据的个数
    protected int count = 0;

    // 数据大小
    protected int length = -1;

    // 该stream是否关闭，被关闭后相关操作会抛出IOException异常
    protected boolean closed = false;

    //
    protected HttpResponse response;

    // 默认false，通过set方法赋值，它决定flush时是否将buffer中数据写入到输出流
    protected boolean commit;

    public ResponseStream(HttpResponse response) {
        closed = false;
        commit = false;
        count = 0;
        this.response = response;
    }

    /**
     * 关闭输出流，buffer中剩余数据会被写入到输出流，
     * 不过之后数据的写入将被拒绝，抛出IOException
     * @throws IOException
     */
    public void close() throws IOException {
        if (closed)
            throw new RuntimeException("responseStream.close.closed");
        response.flushBuffer();
        closed = true;
    }

    public boolean isCommit() {
        return commit;
    }

    public void setCommit(boolean commit) {
        this.commit = commit;
    }

    /**
     * buffer数组数据写入到输出流，受commit字段影响
     * @throws IOException
     */
    public void flush() throws IOException {
        if (closed)
            throw new RuntimeException("responseStream.close.closed");
        if (commit) response.flushBuffer();
    }

    /**
     * 将数据写入到输出流中
     * @param b 数据
     * @throws IOException
     */
    public void write(int b) throws IOException {
        if (closed)
            throw new RuntimeException("responseStream.close.closed");
        if (length > 0 && count >= length)
            throw new IOException("responseStream.write.count");
        response.write(b);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (closed)
            throw new RuntimeException("responseStream.close.closed");
        int actual = len;
        if (length > 0 && count + len >= length)
            actual = length-count;
        response.write(b, off, actual); //调用HttpResponse的实现
        count += actual;
        if (actual < len)
            throw new IOException("responseStream.write.count");
    }
}
