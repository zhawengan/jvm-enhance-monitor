package com.github.zwg.core.netty;

import com.github.zwg.core.command.Command;
import com.github.zwg.core.command.CommandFactory;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.execption.BadCommandException;
import com.github.zwg.core.session.DefaultSessionManager;
import com.github.zwg.core.session.Session;
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
    private final Instrumentation inst;

    public ServerMessageHandler(Instrumentation inst) {
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
        String sessionId = message.getSessionId();
        if (message.getMessageType() == MessageTypeEnum.REGISTER) {
            DefaultSessionManager.getInstance().create(sessionId, channel);
            logger.info("register success. sessionId:{},message:{}", sessionId, message.getBody());
            return;
        }
        if (StringUtils.isBlank(message.getBody())) {
            logger.debug("receive empty message. {}", message);
            return;
        }
        logger.debug("server receive message:{}", message);
        Command command = objectFormat.fromJson(message.getBody(), Command.class);
        CommandHandler commandHandler = CommandFactory.getInstance()
                .getExecuteCommandHandler(command);
        if (commandHandler == null) {
            channel.writeAndFlush(
                    MessageUtil.buildAllResponse(sessionId, "unknown command:" + command.getName()),
                    channel.voidPromise());
            return;
        }
        try {
            Session session = DefaultSessionManager.getInstance().get(sessionId);
            commandHandler.execute(session, inst,
                    result -> channel.writeAndFlush(MessageUtil.buildAllResponse(sessionId, result), channel.voidPromise()));
        } catch (BadCommandException ex) {
            channel.writeAndFlush(MessageUtil.buildAllResponse(sessionId, ex.getMessage()),
                    channel.voidPromise());
        }
    }

}
