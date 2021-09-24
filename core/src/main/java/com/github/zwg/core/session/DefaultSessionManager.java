package com.github.zwg.core.session;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class DefaultSessionManager {

    private final Logger logger = LoggerFactory.getLogger(DefaultSessionManager.class);
    private static DefaultSessionManager instance;
    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private final Map<Channel, Session> channelMap = new ConcurrentHashMap<>();

    private DefaultSessionManager(){

    }

    public Session create(String sessionId, Channel channel) {
        Session session = new Session(sessionId, channel);
        sessionMap.put(sessionId, session);
        channelMap.put(channel, session);
        logger.info("register channel. sessionId:{},channel:{}", sessionId, channel);
        return session;
    }

    public Session get(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public Session get(Channel channel) {
        return channelMap.get(channel);
    }

    public void remove(String sessionId) {
        Session session = sessionMap.remove(sessionId);
        if (session != null) {
            channelMap.remove(session.getChannel());
        }
    }

    public void remove(Channel channel) {
        Session session = channelMap.remove(channel);
        if (session != null) {
            sessionMap.remove(session.getSessionId());
        }
    }

    public static DefaultSessionManager getInstance(){
        if(instance==null){
            synchronized (DefaultSessionManager.class){
                if(instance==null){
                    instance = new DefaultSessionManager();
                }
            }
        }
        return instance;
    }

}
