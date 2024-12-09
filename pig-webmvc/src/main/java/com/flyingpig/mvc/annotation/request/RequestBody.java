package com.flyingpig.mvc.annotation.request;

import java.lang.annotation.*;

/**
 * 标记请求体中的内容需要反序列化
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {
}