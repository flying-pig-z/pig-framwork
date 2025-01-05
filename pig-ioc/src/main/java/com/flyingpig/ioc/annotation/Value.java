package com.flyingpig.ioc.annotation;

import java.lang.annotation.*;

// 属性注入注解
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    String value();
}
