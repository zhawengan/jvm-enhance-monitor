package com.github.zwg.core;

import com.github.zwg.core.manager.ReflectClassManager;
import com.github.zwg.core.netty.ConnServer;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/30
 */
public class AgentListener {

    private final Logger logger = LoggerFactory.getLogger(AgentListener.class);
    private static AgentListener agentListener;
    private final Instrumentation inst;

    private final Boolean isStarted = false;

    private AgentListener(String args, Instrumentation inst) {

        this.inst = inst;
    }

    public Boolean isStarted() {
        return isStarted;
    }

    public synchronized Boolean start() {
        if (!isStarted) {
            ConnServer connServer = new ConnServer(inst);
            connServer.start(8080);
            ReflectClassManager.getInstance().initLoadedClass(Arrays.asList(inst.getAllLoadedClasses()));
            logger.info("agent listener started.");
        }
        return isStarted;
    }


    public static AgentListener getInstance(String args, Instrumentation inst) {
        if (agentListener == null) {
            synchronized (AgentListener.class) {
                if (agentListener == null) {
                    agentListener = new AgentListener(args, inst);
                }
            }
        }
        return agentListener;
    }

}
