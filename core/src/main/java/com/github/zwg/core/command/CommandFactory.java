package com.github.zwg.core.command;

import com.github.zwg.core.util.ClassLoadUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    private static final Map<String, CommandHandler> commandHandlers = new HashMap<>();

    private CommandFactory() {
        Set<Class<?>> classes = ClassLoadUtil
                .scanPackage(CommandFactory.class.getClassLoader(), "com.github.zwg.core.command.handler");

        for (Class<?> clazz:classes) {
            if(CommandHandler.class.isAssignableFrom(clazz)){
                try {
                    CommandHandler handler = (CommandHandler) clazz.newInstance();
                    commandHandlers.put(handler.getCommandName(),handler);
                } catch (Exception e) {
                }
            }
        }
        logger.info("success loaded command handlers:{}", commandHandlers);
    }

    public static CommandFactory getInstance(){
        if(instance==null){
            synchronized (CommandFactory.class){
                if(instance==null){
                    instance = new CommandFactory();
                }
            }
        }
        return instance;
    }

    public CommandHandler getExecuteCommandHandler(Command command) {
        return commandHandlers.get(command.getName());
    }


}
