package com.flyingpig.ioc;

import java.lang.annotation.*;

// 属性注入注解
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Value {
    String value();
}
