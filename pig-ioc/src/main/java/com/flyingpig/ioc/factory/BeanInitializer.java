package com.flyingpig.ioc.factory;

import com.flyingpig.ioc.annotation.PostConstruct;
import com.flyingpig.ioc.annotation.PreDestroy;
import com.flyingpig.ioc.aware.BeanFactoryAware;
import com.flyingpig.ioc.aware.BeanNameAware;
import com.flyingpig.ioc.config.BeanPostProcessor;
import com.flyingpig.ioc.core.SimpleIoC;
import com.flyingpig.ioc.registry.BeanPostProcessorRegistry;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
    Bean 初始化器，负责 Bean 实例的初始化、销毁方法注册以及生命周期回调处理
 */
public class BeanInitializer {

    // IoC 容器实例，用于访问容器中的其他 Bean
    private final SimpleIoC iocContainer;

    // Bean 后处理器注册表，存放所有 Bean 后处理器
    private final BeanPostProcessorRegistry postProcessorRegistry;

    // 存储每个 Bean 的销毁方法，键为 Bean 名称，值为销毁方法
    private final Map<String, Method> destroyMethods = new ConcurrentHashMap<>();

    // 构造方法，初始化 IoC 容器和后处理器注册表
    public BeanInitializer(SimpleIoC iocContainer, BeanPostProcessorRegistry postProcessorRegistry) {
        this.iocContainer = iocContainer;
        this.postProcessorRegistry = postProcessorRegistry;
    }

    /**
     * 初始化 Bean 实例，执行生命周期回调并返回最终的 Bean 实例
     * @param instance Bean 实例
     * @param beanName Bean 名称
     * @return 初始化后的 Bean 实例
     * @throws Exception 如果初始化过程中出现错误
     */
    public Object initializeBean(Object instance, String beanName) throws Exception {
        // 处理实现了 Aware 接口的 Bean
        handleAwareInterfaces(instance, beanName);

        // 执行前置处理器，进行初始化之前的处理
        for (BeanPostProcessor processor : postProcessorRegistry.getBeanPostProcessors()) {
            instance = processor.postProcessBeforeInitialization(instance, beanName);
        }

        // 执行初始化方法 -- @PostConstruct 注解标注的方法
        invokeInitMethod(instance);

        // 执行后置处理器，进行初始化之后的处理
        for (BeanPostProcessor processor : postProcessorRegistry.getBeanPostProcessors()) {
            instance = processor.postProcessAfterInitialization(instance, beanName);
        }

        // 注册销毁方法
        registerDestroyMethod(instance, beanName);

        // 返回初始化后的 Bean 实例
        return instance;
    }

    /**
     * 处理实现了 Aware 接口的 Bean，注入相关信息
     * @param instance Bean 实例
     * @param beanName Bean 名称
     */
    private void handleAwareInterfaces(Object instance, String beanName) {
        // 如果 Bean 实现了 BeanNameAware 接口，则注入 Bean 名称
        if (instance instanceof BeanNameAware) {
            ((BeanNameAware) instance).setBeanName(beanName);
        }

        // 如果 Bean 实现了 BeanFactoryAware 接口，则注入 IoC 容器
        if (instance instanceof BeanFactoryAware) {
            ((BeanFactoryAware) instance).setBeanFactory(iocContainer);
        }
    }

    /**
     * 调用 Bean 的初始化方法，查找 @PostConstruct 注解的方法并执行
     * @param instance Bean 实例
     * @throws Exception 如果调用初始化方法时出现错误
     */
    private void invokeInitMethod(Object instance) throws Exception {
        // 遍历 Bean 的所有方法，查找标有 @PostConstruct 注解的方法
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                method.setAccessible(true); // 确保方法可以访问
                try {
                    // 调用初始化方法
                    method.invoke(instance);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke @PostConstruct method", e);
                }
            }
        }
    }

    /**
     * 注册 Bean 的销毁方法，查找 @PreDestroy 注解标注的销毁方法
     * @param instance Bean 实例
     * @param beanName Bean 名称
     */
    private void registerDestroyMethod(Object instance, String beanName) {
        // 遍历 Bean 的所有方法，查找标有 @PreDestroy 注解的方法
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                method.setAccessible(true); // 确保方法可以访问
                // 注册销毁方法
                destroyMethods.put(beanName, method);
                break; // 每个 Bean 只允许注册一个销毁方法
            }
        }
    }

    /**
     * 执行销毁方法，销毁 Bean 实例
     * @param beanName Bean 名称
     * @param bean Bean 实例
     */
    public void destroyBean(String beanName, Object bean) {
        // 获取该 Bean 的销毁方法
        Method destroyMethod = destroyMethods.get(beanName);
        if (destroyMethod != null) {
            try {
                // 执行销毁方法
                destroyMethod.invoke(bean);
            } catch (Exception e) {
                throw new RuntimeException("Error executing destroy method for bean: " + beanName, e);
            }
        }
        // 移除销毁方法的注册
        destroyMethods.remove(beanName);
    }
}
