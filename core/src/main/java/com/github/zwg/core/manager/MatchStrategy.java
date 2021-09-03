package com.github.zwg.core.manager;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/3
 */
public enum MatchStrategy {
    /**
     * 通配符匹配
     */
    WILDCARD,

    /**
     * 正则表达式匹配
     */
    REGEX,

    /**
     * 字符串全匹配
     */
    EQUALS;
}
