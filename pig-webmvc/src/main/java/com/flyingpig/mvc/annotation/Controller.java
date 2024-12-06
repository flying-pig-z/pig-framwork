package com.flyingpig.mvc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 标注控制器类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Controller {
    String value() default "";
}
