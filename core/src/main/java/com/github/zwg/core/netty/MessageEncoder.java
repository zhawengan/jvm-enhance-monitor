package com.github.zwg.core.netty;

import com.github.zwg.core.util.JacksonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/1 自定义消息编码器
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {


    @Override
    protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out)
            throws Exception {
//        // 这里会判断消息类型是不是EMPTY类型，如果是EMPTY类型，则表示当前消息不需要写入到管道中
//        if (message.getMessageType() != MessageTypeEnum.EMPTY) {
//
//        }
        // 写入当前的魔数
        out.writeInt(Constants.MAGIC_NUMBER);
        // 写入当前的主版本号
        out.writeByte(Constants.MAJOR_VERSION);
        // 写入当前的次版本号
        out.writeByte(Constants.MINOR_VERSION);
        // 写入当前的修订版本号
        out.writeByte(Constants.MODIFY_VERSION);
        if (StringUtils.isBlank(message.getSessionId())) {
            // 生成一个sessionId，并将其写入到字节序列中
            String sessionId = UUID.randomUUID().toString();
            message.setSessionId(sessionId);
        }
        //写入当前sessionId
        out.writeShort(message.getSessionId().length());
        out.writeCharSequence(message.getSessionId(), StandardCharsets.UTF_8);
        // 写入当前消息的类型
        out.writeByte(message.getMessageType().getType());
        // 写入当前消息的附加参数数量
        if (message.getHeaders() == null) {
            message.setHeaders(new HashMap<>());
        }
        String headMsg = JacksonUtil.toJson(message.getHeaders());
        out.writeShort(headMsg.length());
        out.writeCharSequence(headMsg, StandardCharsets.UTF_8);
        //写入消息体
        if (null == message.getBody()) {
            out.writeInt(0);    // 如果消息体为空，则写入0，表示消息体长度为0
        } else {
            out.writeInt(message.getBody().length());
            out.writeCharSequence(message.getBody(), StandardCharsets.UTF_8);
        }

    }
}
