package com.github.zwg.core.command.handler;

import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.asm.EnhanceClassManager;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.netty.MessageUtil;
import com.github.zwg.core.session.DefaultSessionManager;
import com.github.zwg.core.session.Session;
import java.lang.instrument.Instrumentation;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/28
 */
@Cmd(name = ParamConstant.COMMAND_SHUTDOWN, description = "shutdown", help = "shutdown")
public class ShutdownCommandHandler implements CommandHandler {

    @Override
    public void execute(Session session, Instrumentation inst) {
        EnhanceClassManager.getInstance().reset(inst);
        DefaultSessionManager.getInstance().clean();
        session.sendDirectMessage(MessageUtil.buildResponse("Shutdown!"));
        try {
            Thread.sleep(500);
            session.getChannel().parent().close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
