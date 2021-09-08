package com.github.zwg.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Arg {

    /**
     * 参数名称
     */
    String name();

    /**
     * 参数说明
     */
    String description() default "";

    /**
     * 是否必填，与defaultValue可以组合使用
     */
    boolean required() default true;

    /**
     * 默认值
     */
    String defaultValue() default "";
}
