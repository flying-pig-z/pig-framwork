package com.flyingpig.mvc.annotation;

import java.lang.annotation.*;

/**
 * 标记方法返回的是JSON数据，而不是视图
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}
