package com.github.zwg.core.command;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/13
 */
public interface AccessConstant {

    int ACCESS_BEFORE = 1;
    int ACCESS_AFTER_RETUNING = 2;
    int ACCESS_AFTER_THROWING = 4;

    int ACCESS_TRACING_BEFORE = 8;
    int ACCESS_TRACING_RETUNING = 16;
    int ACCESS_TRACING_THROWING = 32;

    static int defaultMethodAccess() {
        return ACCESS_BEFORE + ACCESS_AFTER_RETUNING + ACCESS_AFTER_THROWING;
    }

    static int defaultTracingAccess() {
        return ACCESS_TRACING_BEFORE + ACCESS_TRACING_RETUNING + ACCESS_TRACING_THROWING;
    }

}
