package com.miao.webserver.SecondEdition.connector;

import com.miao.webserver.util.Constants;
import com.miao.webserver.util.StringManager;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 继承自ServletInputStream，在请求实体类Request中使用。如果content length被设置，
 * 则该类中的方法可以确保不会超读数据
 */
public class RequestStream extends ServletInputStream {

    // 是否已关闭
    protected boolean closed;

    // 记录从stream中已读的字符个数
    protected int count;

    // stream中数据总长度
    protected int length = -1;

    // 从该input中读取数据,就是HttpRequest的input
    protected InputStream stream;

    protected static StringManager sm = StringManager.getManager(
            "com.miao.webserver.SecondEdition.connector");


    public RequestStream(HttpRequest request) {
        closed  = false;
        count = 0;
        length = request.getContentLength();
        stream = request.getInput(); // 指的是HttpRequest中被传入的SocketInputStream
    }

    /**
     * 关闭功能， 并没有实际的关闭输入流，而是阻止继续从流中读取数据的操作
     * @throws IOException
     */
    public void close() throws IOException {
        if (closed)
            throw new IOException(sm.getString("requestStream.close.closed"));
        if (length > 0) {
            while (count < length) { //表明没有读完，这里的处理就是吞掉剩下的数据
                if (read() < 0) break;
            }
        }
        closed = true;
    }

    /**
     *  从stream中读取一个字符
     * @return 一个字节大小的数据，或-1代表读完
     * @throws IOException
     */
    public int read() throws IOException {
        if (closed)
            throw new IOException(sm.getString("requestStream.close.closed"));
        // 数据已读完
        if (length >= 0 && count >= length)
            return -1;
        int b = stream.read(); // 调用SocketInputStream的read方法读取一个字符大小的数据
        if (b >= 0) count++; // count表示已经读取的数字量
        return b;
    }

    /**
     * 读取数据,存在byte数组中
     * @param b 存储的数组
     * @return 返回真实读取的数据数
     * @throws IOException
     */
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * 首先判断是否可读，修正所能读取的数据的真实大小，
     * 最终调用InputStream的read(byte b[], int off, int len)方法
     * 该方法会调用read()，在InputStream中它是抽象方法，ServletInputStream并没有实现
     * 所以最终会调用上面的read(）方法， 一个一个字节的从stream中读取数据
     * @param b byte[]
     * @param off 开始位置
     * @param len 长度
     * @return 返回真实读取的数据长度，或-1代表读完
     * @throws IOException
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int toRead = len; // 确定真实可读的数据长度
        if (length > 0) {
            if (count >= length) return -1;
            if (count + len > length)
                toRead = length-count; //修正
        }
        // 调用InputStream的read(byte b[], int off, int len)
        // 该方法返回的是此次读取的字符的个数
        int actuallyRead = super.read(b, off, toRead);
        return actuallyRead;
    }
}
