package com.github.zwg.core.command;

import com.github.zwg.core.session.Session;
import java.lang.instrument.Instrumentation;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public interface CommandHandler {


    void execute(Session session, Instrumentation inst,MonitorCallback callback);

}
