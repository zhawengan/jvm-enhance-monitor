package com.github.zwg.core.command;

import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/2
 */
public interface MonitorCallback {

    void execute(Map<String,Object> result);
}
