package com.github.zwg.core.netty;

import com.github.zwg.core.util.JacksonObjectFormat;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/1 自定义协议解码
 */
public class MessageDecoder extends ByteToMessageDecoder {

    private final JacksonObjectFormat objectFormat = new JacksonObjectFormat();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out)
            throws Exception {
        Message message = new Message();
        message.setMagicNumber(byteBuf.readInt());
        message.setMajorVersion(byteBuf.readByte());
        message.setMinorVersion(byteBuf.readByte());
        message.setModifyVersion(byteBuf.readByte());
        CharSequence charSequence = byteBuf
                .readCharSequence(byteBuf.readShort(), StandardCharsets.UTF_8);
        message.setSessionId((String) charSequence);
        message.setMessageType(MessageTypeEnum.get(byteBuf.readByte()));
        CharSequence headers = byteBuf
                .readCharSequence(byteBuf.readShort(), StandardCharsets.UTF_8);
        Map<String, String> headerData = objectFormat.fromJson((String) headers, Map.class);
        message.getHeaders().putAll(headerData);
        int bodyLength = byteBuf.readInt();
        if (bodyLength > 0) {
            CharSequence body = byteBuf.readCharSequence(bodyLength, StandardCharsets.UTF_8);
            message.setBody(body.toString());
        }
        out.add(message);
    }
}
