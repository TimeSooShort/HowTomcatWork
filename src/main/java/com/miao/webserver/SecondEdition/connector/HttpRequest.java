package com.miao.webserver.SecondEdition.connector;

import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

public class HttpRequest implements HttpServletRequest {

    private InputStream input;
    private String queryString; // 请求参数
    private String requestSessionId;
    private boolean requestSessionURL; // 表示session ID是否来自请求行的URI中
    private boolean requestSessionCookie;  // 表示session ID是否来自请求头cookie
    private String method; //请求方法
    private String requestURI; //请求URI
    private String protocol; // 请求协议
    private int contentLength; // 请求实体主体部分的大小
    private String contentType;


    // map来保存请求头。key：header name ； Value 是list，存储header values
    protected HashMap<String, List<String>> headers = new HashMap<String, List<String>>();

    // 存储cookie
    protected ArrayList<Cookie> cookies = new ArrayList<Cookie>();

    // 用于存储请求参数的 名/值
    // 该类来自org.apache.catalina.util.ParameterMap，它继承自HashMap，其中有一个名为locked
    // 的布尔变量，只有它为false时才可以进行改动(增删改)操作，确保了安全性.
    protected ParameterMap parameters;

    // 请求参数有没有被解析过
    protected boolean parsed;

    // getReader() 方法返回reader
    protected BufferedReader reader;

    // getInputStream（）的返回
    protected ServletInputStream stream;


    // 构造器===========================
    public HttpRequest(InputStream input) {
        this.input = input;
    }



    public void addHeader(String name, String value) {
        name = name.toLowerCase();
        synchronized (headers) {
            List<String> values = headers.get(name);
            if (values == null) {
                values = new ArrayList<String>();
                headers.put(name, values);
            }
            values.add(value);
        }
    }

    public void addCookie(Cookie cookie) {
        synchronized (cookies) {
            cookies.add(cookie);
        }
    }

    /**
     * 解析参数，经过HttpProcessor的parseRequest方法，已经将请求行URI中的参数赋给了queryString
     * 若是POST则参数有可能出现在请求实体中，若两者都有则合并它们。
     * 这里利用Map来存储，key为参数名，value为参数值。使用ParameterMap，
     */
    protected void parseParameters() {
        if (parsed) return;
        ParameterMap paramMap = parameters;
        if (paramMap == null) paramMap = new ParameterMap();
        paramMap.setLocked(false); // 更改map前将locked设为false，否则报错
        String encoding = getCharacterEncoding();
        if (encoding == null) encoding = "ISO-8859-1";

        // 请求行里的参数已经在HttpProcessor的parseRequest被处理好后赋给queryString字段
        String queryString = getQueryString();
        try {
            // 调用RequestUtil来解析
            RequestUtil.parseParameters(paramMap, queryString, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 请求实体里的参数
        String contentType = getContentType();
        if (contentType == null) contentType = "";
        int semicolon = contentType.indexOf(';');
        if (semicolon >= 0)
            contentType = contentType.substring(0, semicolon).trim();
        else
            contentType = contentType.trim();

        // 当方法为POST时，请求体会包含参数，请求头content-length大于0，
        // content-type为application/x-www-form-urlencoded
        if ("POST".equalsIgnoreCase(getMethod()) && getContentLength() > 0
                && "application/x-www-form-urlencoded".equals(contentType)) {
            try {
                int max = getContentLength(); //数据总大小
                // input中请求行与请求头已被读取，接着读取到的就是请求实体了
                // 将请求实体数据存储在该buffer中
                byte[] buffer = new byte[max];
                ServletInputStream sis = getInputStream();
                int len = 0; //记录已读取的数据个数
                // 不断从输入流中读取数据，直到读完，while循环确保数据全部读完
                while (len < max) {
                    // next有两种值，一个代表此次读取的字节个数，另一个-1代表数据已读完
                    int next = sis.read(buffer, len, max-len);
                    if (next < 0) break;
                    len += next;
                }
                sis.close(); //关闭input，并不是实际上的关闭，吞掉剩下的数据并阻止再从sis中读取数据
                if (len < max)
                    throw new RuntimeException("Content length mismatch");
                RequestUtil.parseParameters(paramMap, buffer, encoding);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        paramMap.setLocked(true); //锁住ParameterMap，禁止修改
        parsed = true;
        parameters = paramMap; //赋值parameters字段
    }

    /**
     * 我们实现了一个RequestStream类继承自ServletInputStream
     * 类中实现了close关闭功能，吞掉剩余的数据禁止再从流中读取数据
     * 该类中读取的相关方法会修正要读取的数据大小，以防止超过数据实际大小
     * @return
     */
    public ServletInputStream createInputStream() {
        return new RequestStream(this);
    }

    // 变量的get/set方法=====================
    public InputStream getInput() {
        return input;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRequestSessionId() {
        return requestSessionId;
    }

    public void setRequestSessionId(String requestSessionId) {
        this.requestSessionId = requestSessionId;
    }

    public boolean isRequestSessionURL() {
        return requestSessionURL;
    }

    public void setRequestSessionURL(boolean requestSessionURL) {
        this.requestSessionURL = requestSessionURL;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isRequestSessionCookie() {
        return requestSessionCookie;
    }

    public void setRequestSessionCookie(boolean requestSessionCookie) {
        this.requestSessionCookie = requestSessionCookie;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    // ====================================




    public String getAuthType() {
        return null;
    }

    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    public long getDateHeader(String name) {
        return 0;
    }

    public String getHeader(String name) {
        return null;
    }

    public Enumeration<String> getHeaders(String name) {
        return null;
    }

    public Enumeration<String> getHeaderNames() {
        return null;
    }

    public int getIntHeader(String name) {
        return 0;
    }

    public String getPathInfo() {
        return null;
    }

    public String getPathTranslated() {
        return null;
    }

    public String getContextPath() {
        return null;
    }

    public String getRemoteUser() {
        return null;
    }

    public boolean isUserInRole(String role) {
        return false;
    }

    public Principal getUserPrincipal() {
        return null;
    }

    public String getRequestedSessionId() {
        return null;
    }

    public StringBuffer getRequestURL() {
        return null;
    }

    public String getServletPath() {
        return null;
    }

    public HttpSession getSession(boolean create) {
        return null;
    }

    public HttpSession getSession() {
        return null;
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    public void login(String username, String password) throws ServletException {

    }

    public void logout() throws ServletException {

    }

    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    public Object getAttribute(String name) {
        return null;
    }

    public Enumeration<String> getAttributeNames() {
        return null;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    /**
     * 这里将构造器传入的input封装
     * @return ServletInputStream
     * @throws IOException
     */
    public ServletInputStream getInputStream() throws IOException {
        if (reader != null)
            throw new IllegalStateException("getReader has been called");
        if (stream == null)
            stream = createInputStream();
        return stream;
    }

    public String getParameter(String name) {
        return null;
    }

    public Enumeration<String> getParameterNames() {
        return null;
    }

    public String[] getParameterValues(String name) {
        return new String[0];
    }

    public Map<String, String[]> getParameterMap() {
        return null;
    }

    public String getScheme() {
        return null;
    }

    public String getServerName() {
        return null;
    }

    public int getServerPort() {
        return 0;
    }

    public BufferedReader getReader() throws IOException {
        return null;
    }

    public String getRemoteAddr() {
        return null;
    }

    public String getRemoteHost() {
        return null;
    }

    public void setAttribute(String name, Object o) {

    }

    public void removeAttribute(String name) {

    }

    public Locale getLocale() {
        return null;
    }

    public Enumeration<Locale> getLocales() {
        return null;
    }

    public boolean isSecure() {
        return false;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    public String getRealPath(String path) {
        return null;
    }

    public int getRemotePort() {
        return 0;
    }

    public String getLocalName() {
        return null;
    }

    public String getLocalAddr() {
        return null;
    }

    public int getLocalPort() {
        return 0;
    }

    public ServletContext getServletContext() {
        return null;
    }

    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    public boolean isAsyncStarted() {
        return false;
    }

    public boolean isAsyncSupported() {
        return false;
    }

    public AsyncContext getAsyncContext() {
        return null;
    }

    public DispatcherType getDispatcherType() {
        return null;
    }
}
