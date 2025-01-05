package com.flyingpig.ioc.config;

import com.flyingpig.ioc.core.SimpleIoC;

// BeanFactoryPostProcessor接口：允许修改bean定义
public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(SimpleIoC beanFactory) throws Exception;
}
