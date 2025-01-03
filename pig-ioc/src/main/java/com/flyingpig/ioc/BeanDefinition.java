package com.flyingpig.ioc;

// 3. Bean定义类
class BeanDefinition {
    private Class<?> beanClass;
    private BeanScope scope = BeanScope.SINGLETON;
    private boolean isConstructorInjection = false;
    private String beanName;

    public BeanDefinition(Class<?> beanClass, String beanName) {
        this.beanClass = beanClass;
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    // getters and setters
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
