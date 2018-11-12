package com.miao.webserver.SecondEdition.startup;

import com.miao.webserver.SecondEdition.connector.HttpConnector;

public class BootStrap {

    public static void main(String[] args) {
        HttpConnector connector = new HttpConnector();
        connector.start();
    }
}
