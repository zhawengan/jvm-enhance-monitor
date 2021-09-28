package com.github.zwg.core.advisor;

import lombok.Data;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/9
 */
@Data
public class TracingAdvice {

    private final Integer tracingLineNumber;
    private final String tracingClassName;
    private final String tracingMethodName;
    private final String tracingMethodDesc;

    public TracingAdvice(Integer tracingLineNumber, String tracingClassName,
            String tracingMethodName, String tracingMethodDesc) {

        this.tracingLineNumber = tracingLineNumber;
        this.tracingClassName = tracingClassName;
        this.tracingMethodName = tracingMethodName;
        this.tracingMethodDesc = tracingMethodDesc;
    }
}
