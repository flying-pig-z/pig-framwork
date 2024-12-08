package com.flyingpig.mvc.annotation.mapping;

import java.lang.annotation.*;

/**
 * DELETE请求映射注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = "DELETE")
public @interface DeleteMapping {
    String value() default "";
}
