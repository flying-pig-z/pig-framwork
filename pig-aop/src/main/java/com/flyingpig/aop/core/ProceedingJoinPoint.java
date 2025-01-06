package com.flyingpig.aop.core;

import java.lang.reflect.Method;

/**
 * ProceedingJoinPoint 类实现了 JoinPoint 接口，用于在 AOP 中表示一个连接点（Join Point）。
 * 它封装了目标对象、目标方法以及方法参数，并提供了执行目标方法的能力。
 */
public class ProceedingJoinPoint implements JoinPoint {
    private final Object target;  // 目标对象，即被代理的对象
    private final Method method;  // 目标方法，即被代理的方法
    private final Object[] args;  // 方法参数，即传递给目标方法的参数

    /**
     * 构造函数，初始化 ProceedingJoinPoint 实例。
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param args   方法参数
     */
    public ProceedingJoinPoint(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    /**
     * 执行目标方法，并返回方法的执行结果。
     *
     * @return 目标方法的执行结果
     * @throws Throwable 如果目标方法执行过程中抛出异常
     */
    @Override
    public Object proceed() throws Throwable {
        return method.invoke(target, args);
    }

    /**
     * 获取传递给目标方法的参数。
     *
     * @return 方法参数数组
     */
    @Override
    public Object[] getArgs() {
        return args;
    }

    /**
     * 获取目标方法。
     *
     * @return 目标方法
     */
    @Override
    public Method getMethod() {
        return method;
    }

    /**
     * 获取目标对象。
     *
     * @return 目标对象
     */
    @Override
    public Object getTarget() {
        return target;
    }
}