package com.flyingpig.ioc.resolver;

/*
    值解析器，用于处理@Value注解的值注入
 */
public class DefaultValueResolver implements ValueResolver {
    @Override
    public Object resolveValue(String value, Class<?> type) {
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        } else if (type == Long.class || type == long.class) {
            return Long.parseLong(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        } else if (type == Float.class || type == float.class) {
            return Float.parseFloat(value);
        }
        return value;
    }
}
