package com.flyingpig.aop.test;

import com.flyingpig.aop.annotation.After;
import com.flyingpig.aop.annotation.Around;
import com.flyingpig.aop.annotation.Aspect;
import com.flyingpig.aop.annotation.Before;
import com.flyingpig.aop.core.JoinPoint;
import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
public class LogAspect {

    @Before("@annotation(com.flyingpig.aop.test.Log)")
    public void beforeLog(JoinPoint joinPoint) {
        Method method = joinPoint.getMethod();
        System.out.println("=====================================");
        System.out.println("[Before] 方法开始执行: " + method.getName());
        System.out.println("[Before] 参数列表: " + Arrays.toString(joinPoint.getArgs()));
        System.out.println("[Before] 目标对象: " + joinPoint.getTarget().getClass().getSimpleName());
    }

    @After("@annotation(com.flyingpig.aop.test.Log)")
    public void afterLog(JoinPoint joinPoint) {
        System.out.println("[After] 方法执行完成: " + joinPoint.getMethod().getName());
        System.out.println("=====================================");
    }

    @Around("@annotation(com.flyingpig.aop.test.Log)")
    public Object aroundLog(JoinPoint joinPoint) throws Throwable {
        System.out.println("[Around] 环绕通知开始");
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            System.out.println("[Around] 方法执行时间: " + (endTime - startTime) + "ms");
            System.out.println("[Around] 返回结果: " + result);
            return result;
        } catch (Throwable e) {
            System.out.println("[Around] 发生异常: " + e.getMessage());
            throw e;
        } finally {
            System.out.println("[Around] 环绕通知结束");
        }
    }
}
