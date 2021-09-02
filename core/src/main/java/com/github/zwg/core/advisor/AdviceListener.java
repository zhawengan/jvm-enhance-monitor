package com.github.zwg.core.advisor;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31 通知监听器
 */
public interface AdviceListener {

    void beforeMethod(ClassLoader classLoader, String className, String methodName,
            String methodDesc,
            Object target, Object[] args);

    void afterMethodReturning(ClassLoader classLoader, String className, String methodName,
            String methodDesc,
            Object target, Object[] args, Object returnObject);

    void afterMethodThrowing(ClassLoader classLoader, String className, String methodName,
            String methodDesc,
            Object target, Object[] args, Throwable throwable);

    default void beforeLineInvoking(Integer lineNumber, String className, String methodName,
            String methodDesc) {
    }

    default void afterLineInvoking(Integer lineNumber, String className, String methodName,
            String methodDesc) {
    }

    default void afterLineThrowing(Integer lineNumber, String className, String methodName,
            String methodDesc, String exception) {
    }

}
