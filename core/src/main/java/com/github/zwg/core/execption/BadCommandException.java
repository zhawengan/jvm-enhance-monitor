package com.github.zwg.core.execption;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/3
 */
public class BadCommandException extends RuntimeException {

    public BadCommandException(String message){
        super(message);
    }
}
