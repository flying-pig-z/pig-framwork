package com.flyingpig.mvc.annotation.mapping;

import java.lang.annotation.*;

/**
 * GET请求映射注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = "GET")
public @interface GetMapping {
    String value() default "";
}
