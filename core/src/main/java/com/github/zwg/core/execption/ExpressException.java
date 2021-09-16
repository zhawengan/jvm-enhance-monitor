package com.github.zwg.core.execption;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/9
 */
public class ExpressException extends RuntimeException {

    private final String express;

    public ExpressException(String express, Throwable cause) {
        super(cause);
        this.express = express;
    }

    public String getExpress() {
        return express;
    }
}
