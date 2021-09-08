package com.github.zwg.core.command.handler;

import com.github.zwg.core.advisor.AdviceListenerManager;
import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.MonitorCallback;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.session.DefaultSessionManager;
import com.github.zwg.core.session.Session;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31 退出关闭命令处理器
 */
@Cmd(name = ParamConstant.COMMAND_QUIT, description = "quit", help = "quit")
public class QuitCommandHandler implements CommandHandler {

    @Override
    public void execute(Session session, Instrumentation inst,
            MonitorCallback callback) {
        AdviceListenerManager.unReg(session.getSessionId());
        Map<String,Object> result = new HashMap<>();
        result.put("message","Bye!");
        callback.execute(result);
        DefaultSessionManager.getInstance().remove(session.getChannel());
        session.getChannel().close();
    }
}
