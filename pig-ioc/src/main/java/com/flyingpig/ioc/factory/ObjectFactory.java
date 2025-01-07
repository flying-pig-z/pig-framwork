package com.flyingpig.ioc.factory;


// 添加ObjectFactory接口
@FunctionalInterface
public interface ObjectFactory<T> {
    T getObject() throws Exception;
}