package com.github.zwg.core.netty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/1
 */
public class Message {

    //魔术字
    private int magicNumber;
    //主版本号
    private byte majorVersion;
    //次版本号
    private byte minorVersion;
    //修订版本号
    private byte modifyVersion;
    //会话请求id
    private String sessionId;
    //消息类型
    private MessageTypeEnum messageType;
    //请求头
    private Map<String, String> headers = new HashMap<>();
    //消息体
    private String body;

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public byte getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(byte majorVersion) {
        this.majorVersion = majorVersion;
    }

    public byte getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(byte minorVersion) {
        this.minorVersion = minorVersion;
    }

    public byte getModifyVersion() {
        return modifyVersion;
    }

    public void setModifyVersion(byte modifyVersion) {
        this.modifyVersion = modifyVersion;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public MessageTypeEnum getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageTypeEnum messageType) {
        this.messageType = messageType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Message{" +
                "magicNumber=" + magicNumber +
                ", majorVersion=" + majorVersion +
                ", minorVersion=" + minorVersion +
                ", modifyVersion=" + modifyVersion +
                ", sessionId='" + sessionId + '\'' +
                ", messageType=" + messageType +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
