package com.flyingpig.mvc.annotation.mapping;


import java.lang.annotation.*;

/**
 * 标注请求映射的URL路径
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
    String method() default "";

}

