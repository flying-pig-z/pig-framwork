package com.flyingpig.ioc;

@Component
public class DatabaseConfig implements BeanNameAware, BeanFactoryAware {
    private String host;

    private int port;

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