package com.miao.webserver.SecondEdition.connector;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * 让PrintWriter的所有print与println，write方法执行后都调用flush方法将数据写入到输出流中
 */
public class ResponseWrite extends PrintWriter {

    public ResponseWrite(Writer out) {
        super(out);
    }

    public void print(boolean b) {
        super.print(b);
        super.flush();
    }

    public void print(char c) {
        super.print(c);
        super.flush();
    }

    public void print(char ca[]) {
        super.print(ca);
        super.flush();
    }

    public void print(double d) {
        super.print(d);
        super.flush();
    }

    public void print(float f) {
        super.print(f);
        super.flush();
    }

    public void print(int i) {
        super.print(i);
        super.flush();
    }

    public void print(long l) {
        super.print(l);
        super.flush();
    }

    public void print(Object o) {
        super.print(o);
        super.flush();
    }

    public void print(String s) {
        super.print(s);
        super.flush();
    }

    public void println() {
        super.println();
        super.flush();
    }

    public void println(boolean b) {
        super.println(b);
        super.flush();
    }

    public void println(char c) {
        super.println(c);
        super.flush();
    }

    public void println(char ca[]) {
        super.println(ca);
        super.flush();
    }

    public void println(double d) {
        super.println(d);
        super.flush();
    }

    public void println(float f) {
        super.println(f);
        super.flush();
    }

    public void println(int i) {
        super.println(i);
        super.flush();
    }

    public void println(long l) {
        super.println(l);
        super.flush();
    }

    public void println(Object o) {
        super.println(o);
        super.flush();
    }

    public void println(String s) {
        super.println(s);
        super.flush();
    }
}
