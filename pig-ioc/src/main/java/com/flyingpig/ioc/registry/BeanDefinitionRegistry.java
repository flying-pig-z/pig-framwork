package com.flyingpig.ioc.registry;

import com.flyingpig.ioc.core.BeanDefinition;

/**
 * Bean定义注册表接口
 * 用于管理 Bean 的定义信息，提供注册、获取和清除 BeanDefinition 的功能。
 */
public interface BeanDefinitionRegistry {

    /**
     * 将一个新的 BeanDefinition 注册到容器中
     *
     * @param beanName Bean 的名称，用于唯一标识该 Bean
     * @param beanDefinition Bean 的定义信息，包含类类型、作用域等配置信息
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    /**
     * 根据 Bean 的名称获取对应的 BeanDefinition
     *
     * @param beanName Bean 的名称
     * @return 返回对应名称的 BeanDefinition
     */
    BeanDefinition getBeanDefinition(String beanName);

    /**
     * 清除所有已注册的 BeanDefinition
     * 通常在容器销毁或重新初始化时使用
     */
    void clear();
}
