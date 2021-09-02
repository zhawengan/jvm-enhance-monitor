package com.github.zwg.core.session;


import io.netty.channel.Channel;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 * 用户连接管理
 */
public interface SessionManager {

    Session create(String sessionId, Channel channel);

    Session get(String sessionId);

    Session get(Channel channel);

    void remove(String sessionId);

    void remove(Channel channel);

}
