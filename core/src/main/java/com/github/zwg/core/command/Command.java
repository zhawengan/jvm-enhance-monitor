package com.github.zwg.core.command;

import java.util.Map;
import lombok.Data;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
@Data
public class Command {

    private String name;

    private Map<String, String> options;
}
