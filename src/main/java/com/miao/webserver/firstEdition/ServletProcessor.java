package com.miao.webserver.firstEdition;

import com.miao.webserver.util.Constants;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

public class ServletProcessor {

    public void process(Request request, Response response) {
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf('/')+1);
        URLClassLoader loader = null;

        try {
            URL[] urls = new URL[1];
            File classPath = new File(Constants.WEB_ROOT);
            String repository = (new URL("file", null,
                    classPath.getCanonicalPath() + File.separator)).toString();
            URLStreamHandler streamHandler = null; //用于区分不同构造器
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        Class myClass = null;
        try {
            myClass = loader.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            System.out.println(e.toString());
        }

        Servlet servlet = null;
        try {
            servlet = (Servlet) myClass.newInstance();
            servlet.service((ServletRequest) request, (ServletResponse) response);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
