package com.github.zwg.core.command.handler;

import com.github.zwg.core.command.Command;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.MonitorCallback;
import com.github.zwg.core.session.Session;
import java.lang.instrument.Instrumentation;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 * 输出当前方法执行上下文
 */
public class StackCommandHandler implements CommandHandler {

    @Override
    public String getCommandName() {
        return "stack";
    }

    @Override
    public void execute(Session session, Command command, Instrumentation inst,
            MonitorCallback callback) {

    }
}
