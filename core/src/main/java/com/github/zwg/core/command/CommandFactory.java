package com.github.zwg.core.command;

import com.github.zwg.core.annotation.Arg;
import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.execption.BadCommandException;
import com.github.zwg.core.execption.CommandParamException;
import com.github.zwg.core.util.ClassLoadUtil;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class CommandFactory {

    private static CommandFactory instance;

    private static final Logger logger = LoggerFactory.getLogger(CommandFactory.class);
    private static final Map<String, Class<?>> commandHandlers = new HashMap<>();

    private CommandFactory() {
        Set<Class<?>> classes = ClassLoadUtil
                .scanPackage(CommandFactory.class.getClassLoader(),
                        "com.github.zwg.core.command.handler");

        for (Class<?> clazz : classes) {
            if (CommandHandler.class.isAssignableFrom(clazz) && clazz
                    .isAnnotationPresent(Cmd.class)) {
                Cmd cmd = clazz.getAnnotation(Cmd.class);
                commandHandlers.put(cmd.name(), clazz);
            }
        }
        logger.debug("success loaded command handlers:{}", commandHandlers);
    }

    public static CommandFactory getInstance() {
        if (instance == null) {
            synchronized (CommandFactory.class) {
                if (instance == null) {
                    instance = new CommandFactory();
                }
            }
        }
        return instance;
    }

    public CommandHandler getExecuteCommandHandler(Command command) {
        Class<?> clazz = commandHandlers.get(command.getName());
        return createCommandHandler(clazz, command);
    }

    private CommandHandler createCommandHandler(Class<?> clazz, Command command) {
        if (clazz == null) {
            throw new BadCommandException("bad command:" + command.getName());
        }
        try {
            CommandHandler instance = (CommandHandler) clazz.newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Arg.class)) {
                    continue;
                }
                Arg arg = field.getAnnotation(Arg.class);
                String argVal = arg.defaultValue();
                String argParam = command.getOptions().get(arg.name());
                if (!StringUtils.isBlank(argParam)) {
                    argVal = argParam;
                } else if (arg.required()) {
                    throw new CommandParamException(
                            String.format("command param:%s is required", arg.name()));
                }
                setValue(field, argVal, instance);
            }
            return instance;
        } catch (CommandParamException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("bad command:" + command.getName());
        }
    }

    //this method need to improve
    private void setValue(Field field, String value, Object target) throws IllegalAccessException {
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            String simpleName = field.getType().getSimpleName();
            if ("Integer".equals(simpleName)) {
                field.set(target, Integer.valueOf(value));
            } else {
                field.set(target, value);
            }

        } finally {
            field.setAccessible(accessible);
        }
    }


}
