// 1. 定义需要的注解和接口
package com.flyingpig.ioc;
// 1. 定义需要的注解和接口

// 1. 定义需要的注解和接口

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Bean {
    String value() default "";
}
// 5. 测试类和测试用例

