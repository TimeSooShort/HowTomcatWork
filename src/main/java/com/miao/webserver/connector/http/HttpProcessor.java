package com.miao.webserver.connector.http;

import org.apache.catalina.connector.http.HttpHeader;
import org.apache.catalina.connector.http.HttpRequestLine;
import org.apache.catalina.connector.http.SocketInputStream;
import com.miao.webserver.util.StringManager;
import org.apache.catalina.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProcessor {

    private HttpConnector connector; // 与HttpConnector关联
    private HttpRequest request;
    private HttpRequestLine requesLine = new HttpRequestLine();
    private HttpResponse response;

    protected StringManager sm = StringManager.getManager("com.miao.webserver.connector.http");

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    /**
     * 1,创建一个HttpRequest对象。2，创建一个HttpResponse对象。3，解析Http请求的第一行内容和请求头信息，
     * 填充HttpRequest对象。4，将HttpRequest与HttpResponse传递给ServletProcessor或StaticResourceProcessor的processor()
     * @param socket
     */
    public void process(Socket socket) {
        SocketInputStream input = null;
        OutputStream output = null;
        try {
            input = new SocketInputStream(socket.getInputStream(), 2048);
            output = socket.getOutputStream();

            request = new HttpRequest(input);

            response = new HttpResponse(output);
            response.setRequest(request);

            parseRequest(input);
            parseHeaders(input);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析请求行获取的值赋给HttpRequest对象
     * @param input org.apache.catalina.connector.http.SocketInputStream
     */
    private void parseRequest(SocketInputStream input) throws IOException, ServletException {
        // 解析请求行
        input.readRequestLine(requesLine);
        // 请求方法
        String method = new String(requesLine.method, 0, requesLine.methodEnd);
        // 请求URI
        String uri = null;
        // 协议
        String protocol = new String(requesLine.protocol, 0, requesLine.protocolEnd);

        // validate
        if (method.length() < 1) {
            throw new ServletException("Missing HTTP request method");
        }
        else if (requesLine.uriEnd < 1) {
            throw new ServletException("Missing HTTP request URI");
        }

        // URI中可能含有参数，分情况对HttpRequest中的queryString字段进行赋值
        int question = requesLine.indexOf("?");
        if (question >= 0) {
            request.setQueryString(new String(requesLine.uri, question+1,
                    requesLine.uriEnd-question-1));
            uri = new String(requesLine.uri, 0, question);
        }
        else {
            request.setQueryString(null);
            uri = new String(requesLine.uri, 0, requesLine.uriEnd);
        }

        // URI可能是个完全路径：http://xxxx/servlet/xxx
        if (!uri.startsWith("/")) {
            int pos = uri.indexOf("://");
            if (pos != -1) {
                pos = uri.indexOf('/', pos+3);
                if (pos == -1) {
                    uri = "";
                }
                else {
                    uri = uri.substring(pos);
                }
            }
        }

        // URI中包含session ID，将其剥离
        String match = ";jsessionid=";
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            // 去除等于号后面的session ID
            String rest = uri.substring(semicolon+match.length());
            // 确定其边界
            int semicolon2 = rest.indexOf(";");
            if (semicolon2 >= 0) {
                request.setRequestSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2); // 原先URI中jsessionid之后的剩余部分
            }
            else {
                request.setRequestSessionId(rest);
                rest = "";
            }
            request.setRequestSessionURL(true);
            uri = uri.substring(0, semicolon) + rest; // 剔除jsessionid后的URI
        }
        else {
            request.setRequestSessionURL(false);
            request.setRequestSessionId(null);
        }

        //到这里URI就变成了指向服务器资源的路径，还应该对该路径进行相关的处理
        // 如：\\应该转为/  /./转为/  /../转到父目录等。调用org.apache.catalina.util.RequestUtil
        String normalizeURI = RequestUtil.normalize(uri);

        request.setMethod(method);
        request.setProtocol(protocol);
        if (normalizeURI == null) {
            request.setRequestURI(uri);
            throw new ServletException("Invalid URI: " + uri);
        }
        else {
            request.setRequestURI(normalizeURI);
        }
    }

    /**
     * 解析请求头， 并将相关信息赋给HttpRequest
     * @param input SocketInputStream
     * @throws IOException
     * @throws ServletException
     */
    private void parseHeaders(SocketInputStream input) throws IOException, ServletException {
        // 不断的从SocketInputStream中读取请求头，直到读完
        while (true) {
            // 封装请求头的对象
            HttpHeader header = new HttpHeader();
            // SocketInputStream的readHeader方法每次解析一个请求头，再将信息赋给HttpHeader
            input.readHeader(header);

            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return; //表明请求头已经全部解析完成
                }
                else {
                    throw new ServletException(sm.getString("httpProcessor.parseHeaders.colon"));
                }
            }

            String name = new String(header.name, 0, header.nameEnd);
            String value = new String(header.value, 0, header.valueEnd);
            request.addHeader(name, value);

            // 这里只对cookie，content-length，content-type这些头进行相关处理
            if (name.equalsIgnoreCase("cookie")) {
                // 利用org.apache.catalina.util.RequestUtil来解析cookie的值
                Cookie[] cookies = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equalsIgnoreCase("jsessionid")) {
                        if (!request.isRequestedSessionIdFromCookie()) {
                            request.setRequestSessionId(cookies[i].getValue());
                            request.setRequestSessionCookie(true);
                            request.setRequestSessionURL(false);
                        }
                    }
                    request.addCookie(cookies[i]);
                }
            }
            else if (name.equalsIgnoreCase("content-length")) {
                int n = -1;
                try {
                    n = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new ServletException(sm.getString("httpProcessor.parseHeaders.contentLength"));
                }
                request.setContentLength(n);
            }
            else if (name.equalsIgnoreCase("content-type")) {
                request.setContentType(value);
            }
        }
    }
}
