package com.github.zwg.core.command.handler;

import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.netty.MessageUtil;
import com.github.zwg.core.session.Session;
import java.lang.instrument.Instrumentation;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31 退出关闭命令处理器
 */
@Cmd(name = ParamConstant.COMMAND_QUIT, description = "quit", help = "quit")
public class QuitCommandHandler implements CommandHandler {

    @Override
    public void execute(Session session, Instrumentation inst) {
        session.clean();
        session.sendDirectMessage(MessageUtil.buildResponse("Bye!"));
        session.destroy();
    }
}
