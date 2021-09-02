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
 * 查看对应class或者方法的字节码
 */
public class AsmCommandHandler implements CommandHandler {

    @Override
    public String getCommandName() {
        return "asm";
    }

    @Override
    public void execute(Session session, Command command, Instrumentation inst,
            MonitorCallback callback) {

    }
}
