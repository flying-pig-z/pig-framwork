package com.flyingpig.ioc;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Component {
    String value() default "";
}
