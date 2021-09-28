package com.github.zwg.core.command;

import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/1
 */
public class CommandParse {

    private static final Logger logger = LoggerFactory.getLogger(CommandParse.class);

    public static Command parse(String commandLine) {
        if (!StringUtils.isBlank(commandLine)) {
            try {
                Command command = new Command();
                int paramIndex = commandLine.indexOf("-");
                if (paramIndex < 0) {
                    command.setName(getCommandString(commandLine));
                    return command;
                } else {
                    command.setName(getCommandString(commandLine.substring(0, paramIndex)));
                    command.setOptions(new HashMap<>());
                }
                String[] args = commandLine.split("-");
                for (int i = 1; i < args.length; i++) {
                    int keyIndex = args[i].indexOf(" ");
                    if (keyIndex < 0) {
                        throw new RuntimeException("parse param error. " + args[i]);
                    }
                    String key = args[i].substring(0, keyIndex);
                    String val = args[i].substring(keyIndex);
                    command.getOptions()
                            .put(getCommandString(key), getCommandString(val));
                }
                return command;
            } catch (Exception ex) {
                logger.warn("command analysis error.", ex);
            }
        }
        return null;
    }

    private static String getCommandString(String input) {
        if (StringUtils.isBlank(input)) {
            throw new RuntimeException("parse command name error.");
        }
        return StringUtils.trim(input);
    }

}
