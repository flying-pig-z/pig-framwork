package com.flyingpig.ioc.factory;

import com.flyingpig.ioc.annotation.Autowired;
import com.flyingpig.ioc.annotation.Value;
import com.flyingpig.ioc.resolver.ValueResolver;
import com.flyingpig.ioc.utils.BeanNameUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 负责执行依赖注入，支持字段注入和方法注入
  */
public class DependencyInjector {
    // Bean 工厂，用于获取 Bean 实例
    private final BeanFactory beanFactory;

    // 值解析器，用于解析 @Value 注解中的值
    private final ValueResolver valueResolver;

    // 记录当前正在进行依赖注入的 Bean，用于检测循环依赖
    private final ThreadLocal<Set<String>> injectingBeans = ThreadLocal.withInitial(HashSet::new);

    // 构造方法，初始化 Bean 工厂和值解析器
    public DependencyInjector(BeanFactory beanFactory, ValueResolver valueResolver) {
        this.beanFactory = beanFactory;
        this.valueResolver = valueResolver;
    }

    /**
     * 注入指定实例的依赖，支持字段和方法注入
     * @param instance Bean 实例
     * @param beanClass Bean 的类
     * @param beanName Bean 名称
     * @throws Exception 如果注入过程中发生异常
     */
    public void injectDependencies(Object instance, Class<?> beanClass, String beanName) throws Exception {
        // 判断当前 Bean 是否正在注入，如果正在注入，则说明存在循环依赖，直接返回
        boolean alreadyInjecting = !injectingBeans.get().add(beanName);
        if (alreadyInjecting) {
            // 发现循环依赖，直接返回，等待其他 Bean 完成注入
            return;
        }

        try {
            // 注入字段依赖
            injectFields(instance, beanClass);

            // 注入方法依赖
            injectSetters(instance, beanClass);
        } finally {
            // 完成注入后，移除当前 Bean 名称
            injectingBeans.get().remove(beanName);

            // 如果当前线程中没有其他 Bean 在注入，清理 ThreadLocal
            if (injectingBeans.get().isEmpty()) {
                injectingBeans.remove();
            }
        }
    }

    /**
     * 注入字段依赖，支持 @Autowired 和 @Value 注解
     * @param instance Bean 实例
     * @param beanClass Bean 的类
     * @throws Exception 如果注入过程中发生异常
     */
    private void injectFields(Object instance, Class<?> beanClass) throws Exception {
        // 遍历所有字段，查找注解 @Autowired 和 @Value 的字段
        for (Field field : beanClass.getDeclaredFields()) {
            // 注入 @Autowired 注解标记的字段
            if (field.isAnnotationPresent(Autowired.class)) {
                injectAutowiredField(instance, field);
            }
            // 注入 @Value 注解标记的字段
            else if (field.isAnnotationPresent(Value.class)) {
                injectValueField(instance, field);
            }
        }
    }

    /**
     * 注入 @Autowired 注解标记的字段
     * @param instance Bean 实例
     * @param field 需要注入的字段
     * @throws Exception 如果注入过程中发生异常
     */
    private void injectAutowiredField(Object instance, Field field) throws Exception {
        field.setAccessible(true);  // 设置字段可访问

        // 根据字段的类型推导 Bean 名称，假设采用小写首字母的类名作为 Bean 名称
        String dependencyBeanName = BeanNameUtils.toLowerFirstCase(field.getType().getSimpleName());

        // 从 Bean 工厂获取依赖的 Bean 实例，可能是提前暴露的实例
        Object dependencyBean = beanFactory.getBean(dependencyBeanName);

        // 如果找到依赖 Bean，则注入到当前字段
        if (dependencyBean != null) {
            field.set(instance, dependencyBean);
        }
    }

    /**
     * 注入 @Value 注解标记的字段
     * @param instance Bean 实例
     * @param field 需要注入的字段
     * @throws Exception 如果注入过程中发生异常
     */
    private void injectValueField(Object instance, Field field) throws Exception {
        field.setAccessible(true);  // 设置字段可访问

        // 获取 @Value 注解的值
        Value valueAnnotation = field.getAnnotation(Value.class);

        // 使用值解析器解析值，并注入到字段中
        Object value = valueResolver.resolveValue(valueAnnotation.value(), field.getType());
        field.set(instance, value);
    }

    /**
     * 注入方法依赖，支持 @Autowired 注解标记的 Setter 方法
     * @param instance Bean 实例
     * @param beanClass Bean 的类
     * @throws Exception 如果注入过程中发生异常
     */
    private void injectSetters(Object instance, Class<?> beanClass) throws Exception {
        // 遍历所有方法，查找注解 @Autowired 的 Setter 方法
        for (Method method : beanClass.getDeclaredMethods()) {
            // 如果是符合条件的 Setter 方法，注入依赖
            if (isAutowiredSetter(method)) {
                injectAutowiredSetter(instance, method);
            }
        }
    }

    /**
     * 判断一个方法是否为符合条件的 @Autowired Setter 方法
     * @param method 方法
     * @return 如果是 @Autowired 注解的 Setter 方法，返回 true；否则返回 false
     */
    private boolean isAutowiredSetter(Method method) {
        return method.isAnnotationPresent(Autowired.class) && // 方法上有 @Autowired 注解
                method.getName().startsWith("set") && // 方法名以 "set" 开头
                method.getParameterCount() == 1; // 只有一个参数
    }

    /**
     * 注入 @Autowired 注解标记的 Setter 方法
     * @param instance Bean 实例
     * @param method 需要注入的 Setter 方法
     * @throws Exception 如果注入过程中发生异常
     */
    private void injectAutowiredSetter(Object instance, Method method) throws Exception {
        method.setAccessible(true);  // 设置方法可访问

        // 获取方法参数类型
        Class<?> paramType = method.getParameterTypes()[0];

        // 根据参数类型推导依赖的 Bean 名称，假设采用小写首字母的类名作为 Bean 名称
        String dependencyBeanName = BeanNameUtils.toLowerFirstCase(paramType.getSimpleName());

        // 从 Bean 工厂获取依赖的 Bean 实例，可能是提前暴露的实例
        Object dependencyBean = beanFactory.getBean(dependencyBeanName);

        // 如果找到依赖 Bean，则调用 Setter 方法注入
        if (dependencyBean != null) {
            method.invoke(instance, dependencyBean);
        }
    }
}
