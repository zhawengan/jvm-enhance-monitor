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
 * 退出关闭命令处理器
 */
public class QuitCommandHandler implements CommandHandler {

    @Override
    public String getCommandName() {
        return "quit";
    }

    @Override
    public void execute(Session session, Command command, Instrumentation inst,
            MonitorCallback callback) {

    }
}
