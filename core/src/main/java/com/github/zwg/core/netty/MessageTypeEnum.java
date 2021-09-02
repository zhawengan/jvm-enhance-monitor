package com.github.zwg.core.netty;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/1
 */
public enum MessageTypeEnum {

    REQUEST((byte) 1),
    RESPONSE((byte) 2),
    PING((byte) 3),
    PONG((byte) 4),
    EMPTY((byte) 5);

    private byte type;

    MessageTypeEnum(byte type) {
        this.type = type;
    }

    public byte getType() {
        return this.type;
    }

    public static MessageTypeEnum get(byte type) {
        for (MessageTypeEnum val : values()) {
            if (val.type == type) {
                return val;
            }
        }
        throw new RuntimeException("unsupported type:" + type);
    }
}
