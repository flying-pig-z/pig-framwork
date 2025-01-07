package com.flyingpig.ioc.factory;

import com.flyingpig.ioc.annotation.Autowired;
import com.flyingpig.ioc.core.BeanDefinition;
import com.flyingpig.ioc.utils.BeanNameUtils;

import java.lang.reflect.Constructor;

/**
 * 负责Bean的实例化
 */
public class BeanCreator {
    // BeanFactory 用于获取Bean实例
    private final BeanFactory beanFactory;

    public BeanCreator(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    // 例化Bean -- 使用@Autowired指定的构造函数或者默认第一个构造函数
    public Object createBeanInstance(BeanDefinition beanDefinition) throws Exception {
        // 通过Bean的定义获取Bean的类对象
        Class<?> beanClass = beanDefinition.getBeanClass();

        // 查找带有 @Autowired 注解的构造方法（如果存在）
        Constructor<?> autowiredConstructor = findAutowiredConstructor(beanClass);

        // 如果找到了@Autowired标注的构造函数，则使用该构造函数创建实例
        if (autowiredConstructor != null) {
            return createInstanceWithAutowiredConstructor(autowiredConstructor);
        }

        // 如果没有@Autowired构造函数，则使用默认构造函数创建实例
        return createInstanceWithDefaultConstructor(beanClass);
    }

    // 查找带有 @Autowired 注解的构造函数
    private Constructor<?> findAutowiredConstructor(Class<?> beanClass) {
        // 遍历类的所有构造函数，查找是否有@Autowired注解的构造函数
        for (Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                return constructor;
            }
        }
        // 如果没有找到带@Autowired注解的构造函数，则返回null
        return null;
    }

    // 使用带有 @Autowired 注解的构造函数创建Bean实例
    private Object createInstanceWithAutowiredConstructor(Constructor<?> constructor) throws Exception {
        // 设置构造函数可访问，并获取获取构造函数的参数类型数组
        constructor.setAccessible(true);

        Class<?>[] parameterTypes = constructor.getParameterTypes();

        // 创建一个参数数组存储构造函数需要的所有参数实例
        Object[] parameters = new Object[parameterTypes.length];

        // 遍历参数类型，依次从 BeanFactory 中获取相应的Bean
        for (int i = 0; i < parameterTypes.length; i++) {
            // 将参数名转换为小写字母开头的Bean名称，并从BeanFactory中获取Bean实例
            parameters[i] = beanFactory.getBean(BeanNameUtils.toLowerFirstCase(parameterTypes[i].getSimpleName()));
        }

        // 使用构造函数和参数数组创建Bean实例
        return constructor.newInstance(parameters);
    }

    // 使用默认构造函数创建Bean实例
    private Object createInstanceWithDefaultConstructor(Class<?> beanClass) throws Exception {
        try {
            // 尝试使用无参构造函数创建Bean实例
            return beanClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            // 如果没有无参构造函数，则尝试使用第一个构造函数（如果有的话）
            Constructor<?> constructor = beanClass.getDeclaredConstructors()[0];
            return createInstanceWithAutowiredConstructor(constructor);  // 使用有参构造函数创建实例
        }
    }
}
