package com.github.zwg.core.session;


import io.netty.channel.Channel;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class Session {

    //用户sessionId
    private String sessionId;
    //连接通道
    private Channel channel;

    public Session(String sessionId, Channel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
