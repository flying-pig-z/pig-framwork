package com.flyingpig.ioc.factory;

import com.flyingpig.ioc.core.BeanDefinition;
import com.flyingpig.ioc.core.BeanScope;
import com.flyingpig.ioc.core.SimpleIoC;
import com.flyingpig.ioc.registry.BeanDefinitionRegistry;
import com.flyingpig.ioc.registry.BeanPostProcessorRegistry;
import com.flyingpig.ioc.resolver.DefaultValueResolver;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的 Bean 工厂实现，负责创建、管理和销毁 Bean 实例
 */
public class DefaultBeanFactory implements BeanFactory {

    // BeanDefinition 注册表，存储 Bean 定义信息
    private final BeanDefinitionRegistry beanDefinitionRegistry;

    // 一级缓存：存放完全初始化好的单例 Bean 实例
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    // 二级缓存：存放提前暴露的，尚未进行属性填充和完全初始化的 Bean 实例
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();

    // 三级缓存：存放 Bean 的工厂对象，用于延迟创建代理对象
    private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>();

    // 记录正在创建中的 Bean，避免循环依赖
    private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // 负责Bean的实例化
    private final BeanCreator beanCreator;

    // 依赖注入器，负责注入 Bean 的依赖
    private final DependencyInjector dependencyInjector;

    // 负责Bean的初始化
    private final BeanInitializer beanInitializer;

    public DefaultBeanFactory(BeanDefinitionRegistry beanDefinitionRegistry,
                              BeanPostProcessorRegistry postProcessorRegistry,
                              SimpleIoC iocContainer) {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
        this.beanCreator = new BeanCreator(this);
        this.dependencyInjector = new DependencyInjector(this, new DefaultValueResolver());
        this.beanInitializer = new BeanInitializer(iocContainer, postProcessorRegistry);
    }

    // 获取指定名称的 Bean 实例
    @Override
    public Object getBean(String beanName) throws Exception {
        // 从 BeanDefinition 注册表中获取 Bean 定义
        BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanName);
        if (beanDefinition == null) {
            throw new Exception("No bean named '" + beanName + "' is defined");
        }

        // 如果是单例 Bean，则从缓存中获取或创建
        if (beanDefinition.getScope() == BeanScope.SINGLETON) {
            return getSingletonBean(beanName, () -> createBean(beanDefinition));
        }

        // 否则，直接创建 Bean 实例
        return createBean(beanDefinition);
    }

    // 获取单例 Bean，支持循环依赖的处理
    private Object getSingletonBean(String beanName, ObjectFactory<?> singletonFactory) throws Exception {
        synchronized (this.singletonObjects) {
            // 先尝试从一级缓存中获取已创建好的单例 Bean
            Object singleton = singletonObjects.get(beanName);
            if (singleton != null) {
                return singleton;
            }

            // 判断是否存在循环依赖，若当前正在创建中的 Bean 包含该 Bean 名称，则表示可能发生循环依赖
            if (singletonsCurrentlyInCreation.contains(beanName)) {
                // 尝试从二级缓存中获取提前暴露的 Bean 实例
                singleton = earlySingletonObjects.get(beanName);
                if (singleton != null) {
                    return singleton;
                }

                // 尝试从三级缓存中获取 Bean 的工厂，若存在工厂则通过工厂创建 Bean 实例
                ObjectFactory<?> factory = singletonFactories.get(beanName);
                if (factory != null) {
                    singleton = factory.getObject();
                    // 将实例放入二级缓存并移除三级缓存中的工厂
                    earlySingletonObjects.put(beanName, singleton);
                    singletonFactories.remove(beanName);
                    return singleton;
                }
            }

            // 如果缓存中没有，开始创建 Bean
            try {
                // 将该 Bean 名称标记为正在创建中，防止循环依赖
                singletonsCurrentlyInCreation.add(beanName);
                singleton = singletonFactory.getObject();

                // 创建完成后，放入一级缓存，并移除二级和三级缓存
                singletonObjects.put(beanName, singleton);
                earlySingletonObjects.remove(beanName);
                singletonFactories.remove(beanName);
                return singleton;
            } finally {
                // 无论如何都要移除正在创建中的标记
                singletonsCurrentlyInCreation.remove(beanName);
            }
        }
    }

    // 根据 Bean 定义创建 Bean 实例
    private Object createBean(BeanDefinition beanDefinition) throws Exception {
        String beanName = beanDefinition.getBeanName();

        // 创建原始的 Bean 实例
        Object instance = beanCreator.createBeanInstance(beanDefinition);

        // 如果是单例并且当前正在创建该 Bean，则可能存在循环依赖，提前暴露 Bean 实例
        if (beanDefinition.getScope() == BeanScope.SINGLETON &&
                singletonsCurrentlyInCreation.contains(beanName)) {
            // 将 Bean 实例放入三级缓存中，等待后续处理
            singletonFactories.put(beanName, () -> instance);
        }

        // 注入 Bean 的依赖
        dependencyInjector.injectDependencies(instance, beanDefinition.getBeanClass(), beanName);

        // 初始化 Bean（包括执行后处理器等）
        Object exposedObject = beanInitializer.initializeBean(instance, beanName);

        // 如果存在 AOP 代理，返回的 exposedObject 可能是代理对象
        if (beanDefinition.getScope() == BeanScope.SINGLETON) {
            // 将最终的代理对象放入二级缓存，以便处理循环依赖
            earlySingletonObjects.put(beanName, exposedObject);
        }

        return exposedObject;
    }

    // 销毁所有的单例 Bean 实例，进行清理操作
    @Override
    public void destroyAll() {
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            beanInitializer.destroyBean(entry.getKey(), entry.getValue());
        }
        // 清空所有缓存
        singletonObjects.clear();
        earlySingletonObjects.clear();
        singletonFactories.clear();
        singletonsCurrentlyInCreation.clear();
    }
}
