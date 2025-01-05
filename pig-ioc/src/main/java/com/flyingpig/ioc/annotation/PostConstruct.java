package com.flyingpig.ioc.annotation;

import java.lang.annotation.*;

// 生命周期注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostConstruct {
}
