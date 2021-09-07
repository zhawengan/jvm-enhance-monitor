package com.github.zwg.core.advisor;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/5
 */
public class AdviceWeaver {

    public static void onMethodBefore(String sessionId,
            ClassLoader classLoader,
            String className,
            String methodName,
            String methodDesc,
            Object target,
            Object[] args) {

    }

    public static void onMethodReturn(String sessionId, Object returnObject) {

    }

    public static void onMethodThrow(String sessionId, Throwable throwable) {

    }

    public static void invokingBefore(String sessionId,
            Integer lineNumber,
            String owner,
            String name,
            String desc) {

    }

    public static void invokingReturn(String sessionId,
            Integer lineNumber,
            String owner,
            String name,
            String desc) {

    }

    public static void invokingThrow(String sessionId,
            Integer lineNumber,
            String owner,
            String name,
            String desc,
            String throwException) {

    }

}
