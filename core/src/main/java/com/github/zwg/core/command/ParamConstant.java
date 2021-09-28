package com.github.zwg.core.command;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/3
 */
public interface ParamConstant {

    String COMMAND_ASM = "asm";
    String COMMAND_JVM = "jvm";
    String COMMAND_MONITOR = "monitor";
    String COMMAND_PATH_TRACE = "pathTrace";
    String COMMAND_QUIT = "quit";
    String COMMAND_RESET = "reset";
    String COMMAND_SEARCH_CLASS = "sc";
    String COMMAND_SEARCH_METHOD = "sm";
    String COMMAND_STACK = "stack";
    String COMMAND_TOP_THREAD = "top";
    String COMMAND_TRACE = "trace";
    String COMMAND_WATCH = "watch";
    String COMMAND_EXPORT = "export";
    String COMMAND_SHUTDOWN = "shutdown";

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

    /**
     * 统计周期
     */
    String PERIOD = "p";

    String EXPRESS = "e";

    String WATCH = "w";

    String FILE_LOCATION = "f";

    String EXPRESS_CONDITION = "ed";

    String FIELD_INCLUDE_KEY="fi";
}
