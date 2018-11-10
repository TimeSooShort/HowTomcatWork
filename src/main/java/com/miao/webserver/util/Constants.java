package com.miao.webserver.util;

import java.io.File;

public class Constants {

    public static final String WEB_ROOT = System.getProperty("user.dir") +
            File.separator + "webroot";
    public static final String Package = "com.miao.webserver.util";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 6000;
    public static final int PROCESS_IDLE = 0;
    public static final int PROCESS_ACTIVE = 1;
}
