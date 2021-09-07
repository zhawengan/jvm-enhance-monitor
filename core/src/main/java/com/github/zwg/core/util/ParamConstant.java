package com.github.zwg.core.util;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/3
 */
public interface ParamConstant {

    /**
     * command中获取匹配方式的键
     */
    String REG_KEY = "r";

    /**
     * command中获取类匹配的键
     */
    String CLASS_KEY = "c";
    /**
     * command中获取方法匹配的键
     */
    String METHOD_KEY = "m";
    /**
     * command中获取执行次数的键,或者前n个
     */
    String NUMBER_PARAM = "n";
    /**
     * 方法签名
     */
    String METHOD_DESC = "d";
}
