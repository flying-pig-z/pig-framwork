package com.flyingpig.ioc.test.database;

import com.flyingpig.ioc.annotation.Component;
import com.flyingpig.ioc.annotation.PostConstruct;
import com.flyingpig.ioc.annotation.PreDestroy;
import com.flyingpig.ioc.aware.BeanNameAware;

// 测试生命周期方法
@Component
public class ConnectionManager implements BeanNameAware {
    private String beanName;
    private boolean initialized = false;
    private boolean connected = false;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @PostConstruct
    private void init() {
        System.out.println(String.format("[%s] Initializing connection...", beanName));
        this.initialized = true;
        connect();
    }

    @PreDestroy
    private void cleanup() {
        System.out.println(String.format("[%s] Cleaning up connections...", beanName));
        disconnect();
    }

    @PostConstruct
    public void connect() {
        this.connected = true;
        System.out.println(String.format("[%s] Connected to database", beanName));
    }

    @PreDestroy
    public void disconnect() {
        this.connected = false;
        System.out.println(String.format("[%s] Disconnected from database", beanName));
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isConnected() {
        return connected;
    }
}
