package com.lwd.router_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)//类 枚举 接口
@Retention(RetentionPolicy.CLASS)//编译期
public @interface Router {
    String path();
    String group() default "";
}