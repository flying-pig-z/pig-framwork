package com.flyingpig.ioc.resolver;

// 值解析器接口
public interface ValueResolver {
    Object resolveValue(String value, Class<?> type);
}
