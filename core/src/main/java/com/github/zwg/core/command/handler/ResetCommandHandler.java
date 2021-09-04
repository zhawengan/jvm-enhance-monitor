package com.github.zwg.core.command.handler;

import com.github.zwg.core.asm.EnhanceClassManager;
import com.github.zwg.core.command.Command;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.MonitorCallback;
import com.github.zwg.core.session.Session;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31 将所有增强类恢复到原始状态
 */
public class ResetCommandHandler implements CommandHandler {

    @Override
    public String getCommandName() {
        return "reset";
    }

    @Override
    public void execute(Session session, Command command, Instrumentation inst,
            MonitorCallback callback) {
        EnhanceClassManager.getInstance().reset(inst);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "reset enhance class success.");
        callback.execute(result);
    }
}
