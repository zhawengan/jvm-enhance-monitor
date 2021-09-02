package com.github.zwg.core.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
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
                String[] args = commandLine.split("-");
                Command command = new Command();
                if (StringUtils.isBlank(args[0])) {
                    throw new RuntimeException("parse command name error.");
                }
                command.setName(StringUtils.trim(args[0]));
                command.setOptions(new HashMap<>());
                for (int i = 1; i < args.length; i++) {
                    List<String> params = Arrays.asList(args[i].split(" ")).stream()
                            .filter(o -> !StringUtils.isBlank(o)).collect(
                                    Collectors.toList());
                    if (params.size() != 2) {
                        throw new RuntimeException("parse param error. " + args[i]);
                    }
                    command.getOptions()
                            .put(StringUtils.trim(params.get(0)), StringUtils.trim(params.get(1)));
                }
                return command;
            } catch (Exception ex) {
                logger.warn("command analysis error.", ex);
            }
        }
        return null;
    }

}
