package com.github.zwg.core.manager;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/3
 */
public interface Matcher<T> {

    boolean match(T target);
}
