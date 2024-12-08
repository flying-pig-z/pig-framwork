package com.flyingpig.mvc.annotation.mapping;

import java.lang.annotation.*;

/**
 * PUT请求映射注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = "PUT")
public @interface PutMapping {
    String value() default "";
}
