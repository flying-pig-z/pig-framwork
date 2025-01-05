package com.flyingpig.ioc.test.database;

import com.flyingpig.ioc.aware.BeanFactoryAware;
import com.flyingpig.ioc.aware.BeanNameAware;
import com.flyingpig.ioc.core.SimpleIoC;
import com.flyingpig.ioc.annotation.Component;
import com.flyingpig.ioc.annotation.Value;

@Component("databaseConfig")
public class DatabaseConfig implements BeanNameAware, BeanFactoryAware {
    @Value("localhost:3306")
    private String host;

    @Value("3306")
    private int port;

    @Value("true")
    private boolean useSSL;

    private String beanName;
    private SimpleIoC beanFactory;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void setBeanFactory(SimpleIoC beanFactory) {
        this.beanFactory = beanFactory;
    }

    public String getConfigInfo() {
        return String.format("Database Config (Bean: %s) - Host: %s, Port: %d, SSL: %b",
                beanName, host, port, useSSL);
    }

    public SimpleIoC getBeanFactory() {
        return beanFactory;
    }
}