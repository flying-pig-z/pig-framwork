package com.flyingpig.ioc.core;

/**
 * Bean定义类
 */
public class BeanDefinition {

    // 存储Bean类的类型
    private Class<?> beanClass;

    // 表示Bean的作用域。默认为单例模式
    private BeanScope scope = BeanScope.SINGLETON;

    // 标记该 Bean 是否使用构造器注入。默认值是 false。
    private boolean isConstructorInjection = false;

    // 存储 Bean 的名称，通常用于在容器中唯一标识该 Bean。
    private String beanName;

    public BeanDefinition(Class<?> beanClass, String beanName) {
        this.beanClass = beanClass;
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setScope(BeanScope scope) {
        this.scope = scope;
    }

    public BeanScope getScope() {
        return scope;
    }

    public void setConstructorInjection(boolean isConstructorInjection) {
        this.isConstructorInjection = isConstructorInjection;
    }

    public boolean isConstructorInjection() {
        return isConstructorInjection;
    }
}
