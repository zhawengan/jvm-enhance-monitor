package com.github.zwg.core.netty;

import com.github.zwg.core.command.Command;
import com.github.zwg.core.command.CommandFactory;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.session.DefaultSessionManager;
import com.github.zwg.core.session.Session;
import com.github.zwg.core.util.JacksonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
        Command command = JacksonUtil.fromJson(message.getBody(), Command.class);
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
            commandHandler.execute(session, inst);
            invokeCommandResult(session);
        } catch (Exception ex) {
            channel.writeAndFlush(MessageUtil.buildAllResponse(sessionId, ex.getMessage()),
                    channel.voidPromise());
        }
    }

    private void invokeCommandResult(Session session) {
        Thread thread = Thread.currentThread();
        BlockingQueue<Message> writeQueue = session.getWriteQueue();
        Channel channel = session.getChannel();
        String sessionId = session.getSessionId();
        try {
            AtomicBoolean cmdCompleted = session.getCmdCompleted();
            cmdCompleted.set(false);
            while (!session.getDestroy().get()
                    && !thread.isInterrupted()
                    && !cmdCompleted.get()) {
                Message message = writeQueue.poll(200, TimeUnit.MILLISECONDS);
                if (message == null) {
                    if (cmdCompleted.get()) {
                        session.clean();
                        break;
                    }
                } else {
                    message.setSessionId(sessionId);
                    channel.writeAndFlush(message, channel.voidPromise());
                    if (MessageTypeEnum.PROMPT.equals(message.getMessageType())) {
                        session.clean();
                    }
                }
            }
        } catch (Exception ex) {
            logger.info("session:{} write failed,", session.getSessionId(), ex);
        }
        logger.info("process command result. sessionId:{}", sessionId);

    }


}
