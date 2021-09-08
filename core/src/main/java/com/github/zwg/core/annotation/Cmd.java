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
@Target(ElementType.TYPE)
public @interface Cmd {

    /**
     * 命令的名称
     */
    String name();

    /**
     * 命令的含义说明
     */
    String description() default "";

    /**
     * 命令的使用示例
     */
    String[] help() default {};
}
