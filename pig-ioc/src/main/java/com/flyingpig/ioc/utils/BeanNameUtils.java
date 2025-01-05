package com.flyingpig.ioc.utils;

// Bean名称工具类
public class BeanNameUtils {
    public static String toLowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
