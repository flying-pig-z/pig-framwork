package com.flyingpig.ioc.test.database;

// 测试带参数构造函数的实例化
public class ConfigService {
    private final String configPath;
    private final boolean isTest;

    public ConfigService(String configPath, boolean isTest) {
        this.configPath = configPath;
        this.isTest = isTest;
    }

    public String getConfig() {
        return "Config from " + configPath + (isTest ? " (test)" : " (prod)");
    }
}
