package com.flyingpig.ioc.factory;

// Bean工厂接口
public interface BeanFactory {
    Object getBean(String beanName) throws Exception;

    void destroyAll();
}
