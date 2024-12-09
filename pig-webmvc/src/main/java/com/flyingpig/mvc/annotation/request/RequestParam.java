package com.flyingpig.mvc.annotation.request;

import java.lang.annotation.*;

/**
 * 用于标记请求参数
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value() default "";
}