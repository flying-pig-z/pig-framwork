package com.flyingpig.aop.core;

import java.lang.reflect.Method;

/**
 * JoinPoint 接口定义了 AOP 中连接点（Join Point）的基本操作。
 * 连接点表示程序执行过程中的一个特定点，例如方法调用或字段访问。
 * 该接口提供了获取目标方法、目标对象、方法参数以及执行目标方法的能力。
 */
public interface JoinPoint {

    /**
     * 执行目标方法，并返回方法的执行结果。
     * 该方法通常用于在环绕通知（Around Advice）中手动控制目标方法的执行。
     *
     * @return 目标方法的执行结果
     * @throws Throwable 如果目标方法执行过程中抛出异常
     */
    Object proceed() throws Throwable;

    /**
     * 获取传递给目标方法的参数。
     *
     * @return 方法参数数组
     */
    Object[] getArgs();

    /**
     * 获取目标方法。
     *
     * @return 目标方法
     */
    Method getMethod();

    /**
     * 获取目标对象。
     *
     * @return 目标对象
     */
    Object getTarget();
}