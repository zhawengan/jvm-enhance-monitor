package com.github.zwg.core.netty;

import com.github.zwg.core.command.Command;
import com.github.zwg.core.command.CommandFactory;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.execption.BadCommandException;
import com.github.zwg.core.session.SessionManager;
import com.github.zwg.core.util.JacksonObjectFormat;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.instrument.Instrumentation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class ServerMessageHandler extends SimpleChannelInboundHandler<Message> {

    private final Logger logger = LoggerFactory.getLogger(ServerMessageHandler.class);

    private final JacksonObjectFormat objectFormat = new JacksonObjectFormat();
    private final SessionManager sessionManager;
    private final Instrumentation inst;

    private boolean init = false;

    public ServerMessageHandler(SessionManager sessionManager, Instrumentation inst) {

        this.sessionManager = sessionManager;
        this.inst = inst;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        Channel channel = ctx.channel();
        if (message.getMessageType() == MessageTypeEnum.PING) {
            logger.debug("receive heartbeat ping message. {}", message);
            message.setMessageType(MessageTypeEnum.PONG);
            channel.writeAndFlush(message, channel.voidPromise());
            return;
        }
        logger.debug("server receive message:{}", message);
        if (StringUtils.isBlank(message.getBody())) {
            logger.debug("receive empty message. {}", message);
            return;
        }
        Command command = objectFormat.fromJson(message.getBody(), Command.class);
        CommandHandler commandHandler = CommandFactory.getInstance()
                .getExecuteCommandHandler(command);
        if (commandHandler == null) {
            Message response = new Message();
            response.setMessageType(MessageTypeEnum.RESPONSE);
            response.setBody("unknown command:" + command.getName());
            channel.writeAndFlush(response, channel.voidPromise());
            return;
        }
        try {
            commandHandler.execute(sessionManager.get(channel), command, inst, result -> {
                Message response = new Message();
                response.setMessageType(MessageTypeEnum.RESPONSE);
                response.setBody(objectFormat.toJsonPretty(result));
                channel.writeAndFlush(response, channel.voidPromise());
            });
        } catch (BadCommandException ex) {
            Message response = new Message();
            response.setBody(ex.getMessage());
            response.setMessageType(MessageTypeEnum.RESPONSE);
            channel.writeAndFlush(response, channel.voidPromise());
        }
        sendEmptyMessage(channel);
    }

    private void sendEmptyMessage(Channel channel) {
        Message response = new Message();
        response.setMessageType(MessageTypeEnum.EMPTY);
        channel.writeAndFlush(response, channel.voidPromise());
    }

}
