package com.flyingpig.mvc.annotation.mapping;

import java.lang.annotation.*;

/**
 * POST请求映射注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = "POST")
public @interface PostMapping {
    String value() default "";
}
