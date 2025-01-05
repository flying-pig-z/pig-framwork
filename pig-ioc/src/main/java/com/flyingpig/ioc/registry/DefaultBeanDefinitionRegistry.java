package com.flyingpig.ioc.registry;

import com.flyingpig.ioc.core.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
    管理Bean的定义（注册 获取 销毁）
 */
public class DefaultBeanDefinitionRegistry implements BeanDefinitionRegistry {
    private final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitions.put(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitions.get(beanName);
    }

    @Override
    public void clear() {
        beanDefinitions.clear();
    }
}
