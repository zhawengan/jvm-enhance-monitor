package com.github.zwg.core.command;

import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class Command {

    private String name;

    private Map<String,String> options;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
}
