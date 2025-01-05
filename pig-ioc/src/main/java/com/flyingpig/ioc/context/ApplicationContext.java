package com.flyingpig.ioc.context;

import com.flyingpig.ioc.config.BeanFactoryPostProcessor;
import com.flyingpig.ioc.config.BeanPostProcessor;
import com.flyingpig.ioc.core.SimpleIoC;

import java.util.ArrayList;
import java.util.List;

// 应用上下文类，负责协调整个IoC容器
public class ApplicationContext {
    private final SimpleIoC beanFactory;
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public ApplicationContext() {
        this.beanFactory = new SimpleIoC();
    }

    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
        this.beanFactoryPostProcessors.add(postProcessor);
    }

    public void addBeanPostProcessor(BeanPostProcessor postProcessor) {
        this.beanPostProcessors.add(postProcessor);
    }

    public void refresh() throws Exception {
        // 调用BeanFactoryPostProcessor
        for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
            postProcessor.postProcessBeanFactory(beanFactory);
        }

        // 注册BeanPostProcessor到BeanFactory
        for (BeanPostProcessor postProcessor : beanPostProcessors) {
            beanFactory.addBeanPostProcessor(postProcessor);
        }
    }

    // 委托方法
    public void registerComponent(Class<?> componentClass) {
        beanFactory.registerComponent(componentClass);
    }

    public void registerBean(String beanName, Class<?> beanClass) {
        beanFactory.registerBean(beanName, beanClass);
    }

    public Object getBean(String beanName) throws Exception {
        return beanFactory.getBean(beanName);
    }

    public void close() {
        beanFactory.close();
    }
}
