// 1. 自定义@Aspect注解
package com.flyingpig.aop.core;



import com.flyingpig.aop.annotation.Aspect;

import java.lang.reflect.Proxy;

public class AopProxyFactory {
    public static <T> T createProxy(T target, Object aspect) {
        if (!aspect.getClass().isAnnotationPresent(Aspect.class)) {
            throw new IllegalArgumentException("Aspect class must be annotated with @Aspect");
        }

        ClassLoader classLoader = target.getClass().getClassLoader();
        Class<?>[] interfaces = target.getClass().getInterfaces();
        return (T) Proxy.newProxyInstance(
                classLoader,
                interfaces,
                new AopProxyHandler(target, aspect)
        );
    }
}

