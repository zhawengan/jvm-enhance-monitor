package com.github.zwg.core.command.handler;

import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.asm.EnhanceClassManager;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.netty.MessageUtil;
import com.github.zwg.core.session.Session;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31 将所有增强类恢复到原始状态
 */
@Cmd(name = ParamConstant.COMMAND_RESET, description = "reset", help = "reset")
public class ResetCommandHandler implements CommandHandler {


    @Override
    public void execute(Session session, Instrumentation inst) {
        EnhanceClassManager.getInstance().reset(inst);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "reset enhance class success.");
        session.sendCompleteMessage(MessageUtil.buildResponse(result));
    }
}
