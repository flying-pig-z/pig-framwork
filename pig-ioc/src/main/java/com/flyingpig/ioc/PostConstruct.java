package com.flyingpig.ioc;

import java.lang.annotation.*;

// 生命周期注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface PostConstruct {
}
