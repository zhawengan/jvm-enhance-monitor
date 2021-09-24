package com.github.zwg.core.command;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/2
 */
public class CommandParseTest {

    @Test
    @Ignore
    public void parse() {
        String commandLine = "    ";
        Command command = CommandParse.parse(commandLine);
        assertThat(command).isNull();

        commandLine = "trace  -c  *Test  -m  **Name -n   3";
        command = CommandParse.parse(commandLine);
        assertThat(command.getName()).isEqualTo("trace");
        assertThat(command.getOptions().size()).isEqualTo(3);
    }
}