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
 * 负责输出一个类中的所有方法调用路径
 */
public class PathTraceCommandHandler implements CommandHandler {

    @Override
    public String getCommandName() {
        return "pt";
    }

    @Override
    public void execute(Session session, Command command, Instrumentation inst,
            MonitorCallback callback) {

    }
}
