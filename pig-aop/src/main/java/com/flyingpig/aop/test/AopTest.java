package com.flyingpig.aop.test;


import com.flyingpig.aop.core.AopProxyFactory;

public class AopTest {
    public static void main(String[] args) {
        // 创建业务对象和切面对象
        UserService userService = new UserServiceImpl();
        LogAspect logAspect = new LogAspect();

        // 创建代理
        UserService proxy = AopProxyFactory.createProxy(userService, logAspect);

        // 测试方法调用
        System.out.println("\n=== Testing AOP Proxy ===\n");
        proxy.getUserId("12345");
    }
}
