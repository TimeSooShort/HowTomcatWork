package com.miao.webserver.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 不同包下都有一个properties文件用来记录该包里的错误信息，每个properties文件都是用StringManager类的一个实例来处理的。
 * 一个包里有一个StringManager实例，该实例被包中所有实例共享，所以它是单例类。
 */
public class StringManager {

    // 用于解决国际化问题，不同语言环境下选择不同的properties文件
    private ResourceBundle bundle;

    // 存储各个包下的StringManager实例，key为包名。
    private static HashMap<String, StringManager> managers = new HashMap();

    // 私有化构造器
    private StringManager(String packageName) {
        // 规定每个包下的properties文件取名以LocalStrings为前缀。如LocalStrings.properties，LocalStrings_ja.properties
        String bundleName = packageName + ".LocalStrings";
        // 例如一个包下有三个不同语言的properties文件，会选择哪一个？跟踪源码到Locale.getDefault()方法，注解中的解释是
        // The Java Virtual Machine sets the default locale during startup based on the host environment
        // 意思是会根据机器的语言环境来选择使用哪个文件
        bundle = ResourceBundle.getBundle("LocalStrings");
    }

    /**
     * 获取properties文件中key对应的value值
     * @param key 键
     * @return value
     */
    public String getString(String key) {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        String str = null;
        try {
            // MissingResourceException :if no object for the given key can be found
            str = bundle.getString(key);
        } catch (MissingResourceException e) {
            str = "Cannot found message associated with key '" + key + "'";
        }
        return str;
    }

    /**
     * 根据包名获取该包的StringManager实例，锁是类对象
     * @param packageName 包名
     * @return StringManager
     */
    public synchronized static StringManager getManager(String packageName) {
        StringManager mgr = managers.get(packageName);
        if (mgr == null) {
            mgr = new StringManager(packageName);
            managers.put(packageName, mgr);
        }
        return mgr;
    }
}
