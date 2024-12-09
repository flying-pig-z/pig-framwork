package com.flyingpig.mvc.annotation.request;

import java.lang.annotation.*;

/**
 * 用于标记URL路径中的变量
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathVariable {
    String value() default "";
}
