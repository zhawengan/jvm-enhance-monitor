package com.github.zwg.core.advisor;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31 通知监听器
 */
public interface AdviceListener {

    default void beforeMethod(ClassLoader classLoader, String className, String methodName,
            String methodDesc,
            Object target, Object[] args){
    }

    default void afterMethodReturning(ClassLoader classLoader, String className, String methodName,
            String methodDesc,
            Object target, Object[] args, Object returnObject){
    }

    default void afterMethodThrowing(ClassLoader classLoader, String className, String methodName,
            String methodDesc,
            Object target, Object[] args, Throwable throwable){
    }

    default void beforeTraceInvoking(Integer lineNumber, String className, String methodName,
            String methodDesc) {
    }

    default void afterTraceInvoking(Integer lineNumber, String className, String methodName,
            String methodDesc) {
    }

    default void afterTraceThrowing(Integer lineNumber, String className, String methodName,
            String methodDesc, String exception) {
    }

}
