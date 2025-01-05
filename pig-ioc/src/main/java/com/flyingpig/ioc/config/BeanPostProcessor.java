package com.flyingpig.ioc.config;

// BeanPostProcessor接口：允许在Bean初始化前后修改Bean实例
public interface BeanPostProcessor {
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }
}
