package com.flyingpig.ioc.core;


import com.flyingpig.ioc.annotation.*;
import com.flyingpig.ioc.config.BeanPostProcessor;
import com.flyingpig.ioc.factory.BeanFactory;
import com.flyingpig.ioc.factory.DefaultBeanFactory;
import com.flyingpig.ioc.registry.BeanDefinitionRegistry;
import com.flyingpig.ioc.registry.BeanPostProcessorRegistry;
import com.flyingpig.ioc.registry.DefaultBeanDefinitionRegistry;
import com.flyingpig.ioc.registry.DefaultBeanPostProcessorRegistry;
import com.flyingpig.ioc.utils.BeanNameUtils;

/**
 * SimpleIoC容器实现类
 * 作为IoC容器的主要入口点，负责协调各个组件的工作
 */
public class SimpleIoC {
    /**
     * Bean定义注册表，存储所有Bean的定义信息
     */
    private final BeanDefinitionRegistry beanDefinitionRegistry;

    /**
     * Bean工厂，负责Bean的创建和生命周期管理
     */
    private final BeanFactory beanFactory;

    /**
     * Bean后处理器注册表，存储所有的BeanPostProcessor
     */
    private final BeanPostProcessorRegistry postProcessorRegistry;

    /**
     * 构造函数，初始化IoC容器
     * 创建各个组件的默认实现并建立它们之间的关联
     */
    public SimpleIoC() {
        this.beanDefinitionRegistry = new DefaultBeanDefinitionRegistry();
        this.postProcessorRegistry = new DefaultBeanPostProcessorRegistry();
        this.beanFactory = new DefaultBeanFactory(beanDefinitionRegistry, postProcessorRegistry, this);
    }

    /**
     * 获取指定名称的Bean定义
     * @param beanName Bean的名称
     * @return 对应的Bean定义，如果不存在则返回null
     */
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionRegistry.getBeanDefinition(beanName);
    }

    /**
     * 注册带有@Component注解的类
     * 自动解析注解中的name属性作为beanName，如果未指定则使用类名的首字母小写形式
     *
     * @param clazz 要注册的类
     */
    public void registerComponent(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null) {
            String beanName = component.value().isEmpty() ?
                    BeanNameUtils.toLowerFirstCase(clazz.getSimpleName()) : component.value();
            System.out.println("Registering component: " + beanName);
            registerBeanDefinition(beanName, new BeanDefinition(clazz, beanName));
        }
    }

    /**
     * 手动注册一个Bean
     * @param beanName Bean的名称
     * @param beanClass Bean的类型
     */
    public void registerBean(String beanName, Class<?> beanClass) {
        registerBeanDefinition(beanName, new BeanDefinition(beanClass, beanName));
    }

    /**
     * 向注册表中注册Bean定义
     * @param beanName Bean的名称
     * @param beanDefinition Bean的定义信息
     */
    private void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * 添加Bean后处理器
     * Bean后处理器用于在注册后与Bean初始化前对Bean进行额外处理
     * @param beanPostProcessor 要添加的后处理器
     */
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        postProcessorRegistry.addBeanPostProcessor(beanPostProcessor);
    }

    /**
     * 获取Bean实例，委托给Bean工厂来创建或获取Bean实例
     *
     * @param beanName Bean的名称
     * @return Bean实例
     * @throws Exception 如果获取Bean过程中发生错误
     */
    public Object getBean(String beanName) throws Exception {
        return beanFactory.getBean(beanName);
    }

    /**
     * 关闭IoC容器，执行所有单例Bean的销毁方法，清理所有注册表
     */
    public void close() {
        beanFactory.destroyAll();
        beanDefinitionRegistry.clear();
        postProcessorRegistry.clear();
    }
}