package com.github.zwg.core.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/1
 */
public class ClientMessageHandler extends SimpleChannelInboundHandler<Message> {

    private final Logger logger = LoggerFactory.getLogger(ClientMessageHandler.class);
    private final PrintWriter writer;
    private final String sessionId;

    public ClientMessageHandler(String sessionId, PrintWriter writer) {

        this.writer = writer;
        this.sessionId = sessionId;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        //忽略PONG消息
        if (message.getMessageType() == MessageTypeEnum.PONG) {
            logger.debug("client receive message:{}", message);
            return;
        }
        if (message.getMessageType() == MessageTypeEnum.RESPONSE) {
            writer.println(message.getBody());
            writer.flush();
            printPrompt();
        } else {
            printPrompt();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                // 一定时间内，当前服务没有发生读取事件，也即没有消息发送到当前服务来时，
                // 其会发送一个Ping消息到服务器，以等待其响应Pong消息
                Message message = new Message();
                message.setSessionId(sessionId);
                message.setMessageType(MessageTypeEnum.PING);
                ctx.writeAndFlush(message);
            } else if (event.state() == IdleState.WRITER_IDLE) {
                // 如果当前服务在指定时间内没有写入消息到管道，则关闭当前管道
                ctx.close();
            }
        }
    }

    private void printPrompt() {
        writer.println();
        writer.print(Constants.PROMPT);
        writer.flush();
    }
}
