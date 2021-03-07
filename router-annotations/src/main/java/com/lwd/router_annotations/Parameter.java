package com.lwd.router_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @AUTHOR lianwd
 * @TIME 3/6/21
 * @DESCRIPTION TODO
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Parameter {
    String name() default "";
}
