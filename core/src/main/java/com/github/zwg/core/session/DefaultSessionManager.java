package com.github.zwg.core.session;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class DefaultSessionManager implements SessionManager {

    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private final Map<Channel, Session> channelMap = new ConcurrentHashMap<>();

    @Override
    public Session create(String sessionId, Channel channel) {
        Session session = sessionMap.putIfAbsent(sessionId, new Session(sessionId, channel));
        channelMap.putIfAbsent(channel, session);
        return session;
    }

    @Override
    public Session get(String sessionId) {
        return sessionMap.get(sessionId);
    }

    @Override
    public Session get(Channel channel) {
        return channelMap.get(channel);
    }

    @Override
    public void remove(String sessionId) {
        Session session = sessionMap.remove(sessionId);
        if (session != null) {
            channelMap.remove(session.getChannel());
        }
    }

    @Override
    public void remove(Channel channel) {
        Session session = channelMap.remove(channel);
        if (session != null) {
            sessionMap.remove(session.getSessionId());
        }
    }

}
