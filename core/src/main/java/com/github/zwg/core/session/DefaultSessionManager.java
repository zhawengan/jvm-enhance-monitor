package com.github.zwg.core.session;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class DefaultSessionManager {

    private static DefaultSessionManager instance;
    private final Logger logger = LoggerFactory.getLogger(DefaultSessionManager.class);
    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private DefaultSessionManager() {

    }

    public static DefaultSessionManager getInstance() {
        if (instance == null) {
            synchronized (DefaultSessionManager.class) {
                if (instance == null) {
                    instance = new DefaultSessionManager();
                }
            }
        }
        return instance;
    }

    public Session create(String sessionId, Channel channel) {
        Session session = new Session(sessionId, channel);
        sessionMap.put(sessionId, session);
        logger.info("register channel. sessionId:{},channel:{}", sessionId, channel);
        return session;
    }

    public Session get(String sessionId) {
        return sessionMap.get(sessionId);
    }


    public void remove(String sessionId) {
        sessionMap.remove(sessionId);
    }

    public void remove(Channel channel) {
        Session session = null;
        for (Entry<String, Session> item : sessionMap.entrySet()) {
            if (item.getValue().getChannel().equals(channel)) {
                session = item.getValue();
            }
        }
        if (session != null) {
            remove(session.getSessionId());
        }
    }

    public void clean(){
        sessionMap.clear();
    }

}
