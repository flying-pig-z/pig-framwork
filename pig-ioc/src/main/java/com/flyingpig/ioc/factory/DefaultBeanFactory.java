package com.flyingpig.ioc.factory;

import com.flyingpig.ioc.aware.BeanFactoryAware;
import com.flyingpig.ioc.aware.BeanNameAware;
import com.flyingpig.ioc.registry.BeanPostProcessorRegistry;
import com.flyingpig.ioc.annotation.Autowired;
import com.flyingpig.ioc.annotation.PostConstruct;
import com.flyingpig.ioc.annotation.PreDestroy;
import com.flyingpig.ioc.annotation.Value;
import com.flyingpig.ioc.config.BeanPostProcessor;
import com.flyingpig.ioc.core.BeanScope;
import com.flyingpig.ioc.core.BeanDefinition;
import com.flyingpig.ioc.core.SimpleIoC;
import com.flyingpig.ioc.registry.BeanDefinitionRegistry;
import com.flyingpig.ioc.resolver.DefaultValueResolver;
import com.flyingpig.ioc.resolver.ValueResolver;
import com.flyingpig.ioc.utils.BeanNameUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean工厂的默认实现类
 * 负责Bean的创建、初始化、依赖注入和生命周期管理
 */
public class DefaultBeanFactory implements BeanFactory {
    // Bean定义注册表，存储所有Bean的定义信息
    private final BeanDefinitionRegistry beanDefinitionRegistry;

    // Bean后处理器注册表，存储所有的BeanPostProcessor
    private final BeanPostProcessorRegistry postProcessorRegistry;

    // 单例Bean的缓存，存储所有已创建的单例Bean实例
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    // 存储Bean的销毁方法，用于容器关闭时的清理工作
    private final Map<String, Method> destroyMethods = new ConcurrentHashMap<>();

    // 值解析器，用于处理@Value注解的值注入
    private final ValueResolver valueResolver;

    // IoC容器的引用，用于BeanFactoryAware接口的注入
    private final SimpleIoC iocContainer;

    /**
     * 构造函数
     * @param beanDefinitionRegistry Bean定义注册表
     * @param postProcessorRegistry Bean后处理器注册表
     * @param iocContainer IoC容器引用
     */
    public DefaultBeanFactory(BeanDefinitionRegistry beanDefinitionRegistry,
                              BeanPostProcessorRegistry postProcessorRegistry,
                              SimpleIoC iocContainer) {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
        this.postProcessorRegistry = postProcessorRegistry;
        this.iocContainer = iocContainer;
        this.valueResolver = new DefaultValueResolver();
    }

    /**
     * 获取Bean实例
     * @param beanName Bean的名称
     * @return Bean实例
     * @throws Exception 如果Bean不存在或创建过程中发生错误
     */
    @Override
    public Object getBean(String beanName) throws Exception {
        BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanName);
        if (beanDefinition == null) {
            throw new Exception("No bean named '" + beanName + "' is defined");
        }

        // 根据Bean的作用域选择创建方式
        if (beanDefinition.getScope() == BeanScope.SINGLETON) {
            return getSingletonBean(beanName, beanDefinition);
        }

        return createBean(beanDefinition);
    }

    /**
     * 获取或创建单例Bean
     * 使用双重检查锁定模式确保线程安全
     */
    private Object getSingletonBean(String beanName, BeanDefinition beanDefinition) throws Exception {
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject != null) {
            return singletonObject;
        }
        synchronized (this.singletonObjects) {
            singletonObject = singletonObjects.get(beanName);
            if (singletonObject == null) {
                singletonObject = createBean(beanDefinition);
                singletonObjects.put(beanName, singletonObject);
            }
            return singletonObject;
        }
    }

    /**
     * 创建Bean实例
     */
    private Object createBean(BeanDefinition beanDefinition) throws Exception {
        // 实例化Bean -- 使用@Autowired指定的构造函数或者默认第一个构造函数
        Object instance = createBeanInstance(beanDefinition);

        // 注入依赖 -- @Autowired注入对象 @Value注解注入属性
        injectDependencies(instance, beanDefinition.getBeanClass());

        // 处理Aware接口 -- 利用反射得到Bean有没有继承Aware接口，有则注入对应的对象
        handleAwareInterfaces(instance, beanDefinition.getBeanName());

        // 初始化Bean -- 调用PostConstruct注解的初始化方法，并进行初始化的前置处理和后置处理
        instance = initializeBean(instance, beanDefinition.getBeanName());

        // 注册销毁方法 -- 调用@PreDestroy注解的销毁方法销毁Bean
        registerDestroyMethod(instance, beanDefinition.getBeanName());

        return instance;
    }

    /**
     * 创建Bean实例，支持@Autowired构造函数注入和默认构造函数
     */
    private Object createBeanInstance(BeanDefinition beanDefinition) throws Exception {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Constructor<?> autowiredConstructor = findAutowiredConstructor(beanClass);

        if (autowiredConstructor != null) {
            return createInstanceWithAutowiredConstructor(autowiredConstructor);
        }

        return createInstanceWithDefaultConstructor(beanClass);
    }

    /**
     * 查找带有@Autowired注解的构造函数
     */
    private Constructor<?> findAutowiredConstructor(Class<?> beanClass) {
        for (Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                return constructor;
            }
        }
        return null;
    }

    /**
     * 使用带有@Autowired注解的构造函数创建实例
     * 会递归解析构造函数的参数依赖
     */
    private Object createInstanceWithAutowiredConstructor(Constructor<?> constructor) throws Exception {
        constructor.setAccessible(true);
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = getBean(BeanNameUtils.toLowerFirstCase(parameterTypes[i].getSimpleName()));
        }

        return constructor.newInstance(parameters);
    }

    /**
     * 使用默认构造函数创建实例
     * 如果没有默认构造函数，则使用第一个可用的构造函数
     */
    private Object createInstanceWithDefaultConstructor(Class<?> beanClass) throws Exception {
        try {
            return beanClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            Constructor<?> constructor = beanClass.getDeclaredConstructors()[0];
            return createInstanceWithAutowiredConstructor(constructor);
        }
    }

    /**
     * 注入依赖
     * 包括字段注入(@Autowired, @Value)和setter方法注入
     */
    private void injectDependencies(Object instance, Class<?> beanClass) throws Exception {
        // 字段注入
        for (Field field : beanClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                injectAutowiredField(instance, field);
            } else if (field.isAnnotationPresent(Value.class)) {
                injectValueField(instance, field);
            }
        }

        // Setter方法注入
        for (Method method : beanClass.getDeclaredMethods()) {
            if (isAutowiredSetter(method)) {
                injectAutowiredSetter(instance, method);
            }
        }
    }

    /**
     * 注入@Autowired注解的字段
     */
    private void injectAutowiredField(Object instance, Field field) throws Exception {
        field.setAccessible(true);
        field.set(instance, getBean(BeanNameUtils.toLowerFirstCase(field.getType().getSimpleName())));
    }

    /**
     * 注入@Value注解的字段
     */
    private void injectValueField(Object instance, Field field) throws Exception {
        field.setAccessible(true);
        Value valueAnnotation = field.getAnnotation(Value.class);
        Object value = valueResolver.resolveValue(valueAnnotation.value(), field.getType());
        field.set(instance, value);
    }

    /**
     * 判断方法是否为@Autowired的setter方法
     */
    private boolean isAutowiredSetter(Method method) {
        return method.isAnnotationPresent(Autowired.class) &&
                method.getName().startsWith("set") &&
                method.getParameterCount() == 1;
    }

    /**
     * 注入@Autowired注解的setter方法
     */
    private void injectAutowiredSetter(Object instance, Method method) throws Exception {
        method.setAccessible(true);
        Class<?> paramType = method.getParameterTypes()[0];
        method.invoke(instance, getBean(BeanNameUtils.toLowerFirstCase(paramType.getSimpleName())));
    }

    /**
     * 处理Aware接口，如果Bean实现了Aware接口，注入相应的依赖
     */
    private void handleAwareInterfaces(Object instance, String beanName) {
        if (instance instanceof BeanNameAware) {
            ((BeanNameAware) instance).setBeanName(beanName);
        }
        if (instance instanceof BeanFactoryAware) {
            ((BeanFactoryAware) instance).setBeanFactory(iocContainer);
        }
    }

    /**
     * 初始化Bean，包括BeanPostProcessor的前置处理、初始化方法调用、BeanPostProcessor的后置处理
     */
    private Object initializeBean(Object instance, String beanName) throws Exception {
        // 前置处理
        for (BeanPostProcessor processor : postProcessorRegistry.getBeanPostProcessors()) {
            instance = processor.postProcessBeforeInitialization(instance, beanName);
        }

        // 初始化方法
        invokeInitMethod(instance);

        // 后置处理
        for (BeanPostProcessor processor : postProcessorRegistry.getBeanPostProcessors()) {
            instance = processor.postProcessAfterInitialization(instance, beanName);
        }

        return instance;
    }

    /**
     * 调用@PostConstruct注解的初始化方法
     */
    private void invokeInitMethod(Object instance) throws Exception {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                method.setAccessible(true);
                try {
                    method.invoke(instance);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke @PostConstruct method", e);
                }
            }
        }
    }

    /**
     * 注册@PreDestroy注解的销毁方法
     */
    private void registerDestroyMethod(Object instance, String beanName) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                method.setAccessible(true);
                destroyMethods.put(beanName, method);
                break;
            }
        }
    }

    /**
     * 销毁所有单例Bean
     * 调用它们的销毁方法并清理容器
     */
    @Override
    public void destroyAll() {
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();
            Method destroyMethod = destroyMethods.get(beanName);

            if (destroyMethod != null) {
                try {
                    destroyMethod.invoke(bean);
                } catch (Exception e) {
                    throw new RuntimeException("Error executing destroy method for bean: " + beanName, e);
                }
            }
        }

        singletonObjects.clear();
        destroyMethods.clear();
    }
}