package com.github.zwg.core.manager;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/5
 */
public class JemMethod {

    private final String name;
    private final String desc;

    public JemMethod(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "JemMethod{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
