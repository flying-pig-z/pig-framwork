package com.flyingpig.aop.core;


import com.flyingpig.aop.annotation.After;
import com.flyingpig.aop.annotation.Around;
import com.flyingpig.aop.annotation.Before;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AopProxyHandler 类实现了 InvocationHandler 接口，用于在动态代理中处理目标对象的方法调用。
 * 它支持在目标方法执行前后以及环绕执行时插入切面逻辑（如 @Before、@After、@Around 注解标记的方法）。
 */
public class AopProxyHandler implements InvocationHandler {
    private final Object target;  // 目标对象，即被代理的对象
    private final Map<String, Method> beforeMethods;  // 存储 @Before 注解标记的方法
    private final Map<String, Method> afterMethods;   // 存储 @After 注解标记的方法
    private final Map<String, Method> aroundMethods;  // 存储 @Around 注解标记的方法
    private final Object aspect;  // 切面对象，包含切面逻辑的方法

    /**
     * 构造函数，初始化 AopProxyHandler 实例。
     *
     * @param target 目标对象
     * @param aspect 切面对象
     */
    public AopProxyHandler(Object target, Object aspect) {
        this.target = target;
        this.aspect = aspect;
        this.beforeMethods = new HashMap<>();
        this.afterMethods = new HashMap<>();
        this.aroundMethods = new HashMap<>();
        initializeMethods();  // 初始化切面方法
    }

    /**
     * 初始化切面方法，扫描切面类中的所有方法，并根据注解类型将它们存储到对应的Map中
     */
    private void initializeMethods() {
        // 获取目标类的所有方法
        Method[] targetMethods = target.getClass().getMethods();
        // 获取切面类的所有方法
        Method[] aspectMethods = aspect.getClass().getMethods();

        for (Method aspectMethod : aspectMethods) {
            // 处理@Before注解
            if (aspectMethod.isAnnotationPresent(Before.class)) {
                Before beforeAnnotation = aspectMethod.getAnnotation(Before.class);
                String expression = beforeAnnotation.value();
                List<Method> matchedMethods = PointcutExpressionParser.getMatchedMethods(
                        targetMethods, expression);
                for (Method targetMethod : matchedMethods) {
                    beforeMethods.put(targetMethod.getName(), aspectMethod);
                }
            }

            // 处理@After注解
            if (aspectMethod.isAnnotationPresent(After.class)) {
                After afterAnnotation = aspectMethod.getAnnotation(After.class);
                String expression = afterAnnotation.value();
                List<Method> matchedMethods = PointcutExpressionParser.getMatchedMethods(
                        targetMethods, expression);
                for (Method targetMethod : matchedMethods) {
                    afterMethods.put(targetMethod.getName(), aspectMethod);
                }
            }

            // 处理@Around注解
            if (aspectMethod.isAnnotationPresent(Around.class)) {
                Around aroundAnnotation = aspectMethod.getAnnotation(Around.class);
                String expression = aroundAnnotation.value();
                List<Method> matchedMethods = PointcutExpressionParser.getMatchedMethods(
                        targetMethods, expression);
                for (Method targetMethod : matchedMethods) {
                    aroundMethods.put(targetMethod.getName(), aspectMethod);
                }
            }
        }
    }

    /**
     * 代理方法调用的核心逻辑。当代理对象的方法被调用时，会执行此方法。
     *
     * @param proxy  代理对象
     * @param method 被调用的方法
     * @param args   方法参数
     * @return 目标方法的执行结果
     * @throws Throwable 如果目标方法或切面方法执行过程中抛出异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取目标类的实际方法（而不是代理类的方法）
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());

        String methodName = method.getName();  // 获取目标方法名
        ProceedingJoinPoint joinPoint = new ProceedingJoinPoint(target, method, args);  // 创建连接点

        Object result;
        try {
            // 执行 @Before 方法（如果存在）
            Method beforeMethod = beforeMethods.get(methodName);
            if (beforeMethod != null) {
                beforeMethod.invoke(aspect, joinPoint);
            }

            // 执行 @Around 方法（如果存在），否则直接执行目标方法
            Method aroundMethod = aroundMethods.get(methodName);
            if (aroundMethod != null) {
                result = aroundMethod.invoke(aspect, joinPoint);
            } else {
                result = joinPoint.proceed();
            }

            // 执行 @After 方法（如果存在）
            Method afterMethod = afterMethods.get(methodName);
            if (afterMethod != null) {
                afterMethod.invoke(aspect, joinPoint);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();  // 抛出目标方法或切面方法的实际异常
        }

        return result;  // 返回目标方法的执行结果
    }
}