package com.github.zwg.core.advisor;

import com.github.zwg.core.command.AccessConstant;
import java.util.Arrays;
import lombok.Data;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/9
 */
@Data
public class Advice {

    private String traceId;
    private ClassLoader classLoader;
    private String className;
    private String methodName;
    private String methodDesc;
    private Object target;
    private Object[] params;
    private Object returnObj;
    private Throwable throwable;
    private boolean isBefore;
    private boolean isThrow;
    private boolean isReturn;

    private boolean isTracingBefore;
    private boolean isTracingThrowing;
    private boolean isTracingReturning;

    private int access;

    private TracingAdvice tracingAdvice;

    public Advice(ClassLoader classLoader, String className, String methodName, String methodDesc,
            Object target,
            Object[] params, Object returnObj, Throwable throwable, int access) {

        this.classLoader = classLoader;
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.target = target;
        this.params = params;
        this.returnObj = returnObj;
        this.throwable = throwable;
        this.access = access;
        this.isBefore = (access & AccessConstant.ACCESS_BEFORE) == AccessConstant.ACCESS_BEFORE;
        this.isThrow = (access & AccessConstant.ACCESS_AFTER_THROWING)
                == AccessConstant.ACCESS_AFTER_THROWING;
        this.isReturn = (access & AccessConstant.ACCESS_AFTER_RETUNING)
                == AccessConstant.ACCESS_AFTER_RETUNING;

        this.isTracingBefore = (access & AccessConstant.ACCESS_TRACING_BEFORE)
                == AccessConstant.ACCESS_TRACING_BEFORE;
        this.isTracingThrowing = (access & AccessConstant.ACCESS_TRACING_THROWING)
                == AccessConstant.ACCESS_TRACING_THROWING;
        this.isTracingReturning = (access & AccessConstant.ACCESS_TRACING_RETUNING)
                == AccessConstant.ACCESS_TRACING_RETUNING;
    }

    @Override
    public String toString() {
        return "Advice{" +
                "traceId='" + traceId + '\'' +
                ", classLoader=" + classLoader +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodDesc='" + methodDesc + '\'' +
                ", target=" + target +
                ", params=" + Arrays.toString(params) +
                ", returnObj=" + returnObj +
                ", throwable=" + throwable +
                ", isBefore=" + isBefore +
                ", isThrow=" + isThrow +
                ", isReturn=" + isReturn +
                ", isTracingBefore=" + isTracingBefore +
                ", isTracingThrowing=" + isTracingThrowing +
                ", isTracingReturning=" + isTracingReturning +
                ", access=" + access +
                ", tracingAdvice=" + tracingAdvice +
                '}';
    }
}
