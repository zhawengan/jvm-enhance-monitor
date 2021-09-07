package com.github.zwg.agent;

import java.lang.reflect.Method;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/5 在做内部method的内部监控时，需要通过字节码在方法中插入自定义的逻辑 所以需要确保修改后的字节码中能找到插入逻辑的类，改类需要被AppClassLoader加载
 */
public class MonitorProxy {

    /**
     * 方法执行之前调用
     */
    public static Method ON_METHOD_BEFORE;
    /**
     * 方法返回时调用
     */
    public static Method ON_METHOD_RETURN;

    /**
     * 当方法抛出异常
     */
    public static Method ON_METHOD_THROW;

    /**
     * 方法或字节码执行之前调用
     */
    public static Method INVOKING_BEFORE;

    /**
     * 方法或字节码调用之后
     */
    public static Method INVOKING_RETURN;

    /**
     * 方法或字节码异常
     */
    public static Method INVOKING_THROW;

    public static void init(Method onMethodBefore, Method onMethodReturn, Method onMethodThrow,
            Method invokingBefore, Method invokingReturn, Method invokingThrow) {
        ON_METHOD_BEFORE = onMethodBefore;
        ON_METHOD_RETURN = onMethodReturn;
        ON_METHOD_THROW = onMethodThrow;
        INVOKING_BEFORE = invokingBefore;
        INVOKING_RETURN = invokingReturn;
        INVOKING_THROW = invokingThrow;
    }


    public static void clean() {
        ON_METHOD_BEFORE = null;
        ON_METHOD_RETURN = null;
        ON_METHOD_THROW = null;
        INVOKING_BEFORE = null;
        INVOKING_RETURN = null;
        INVOKING_THROW = null;
    }

}
