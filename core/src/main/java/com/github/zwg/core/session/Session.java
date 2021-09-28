package com.github.zwg.core.session;


import com.github.zwg.core.advisor.AdviceListenerManager;
import com.github.zwg.core.netty.Message;
import com.github.zwg.core.netty.MessageUtil;
import io.netty.channel.Channel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
@Data
public class Session {

    //用户sessionId
    private String sessionId;
    //连接通道
    private Channel channel;

    private AtomicBoolean interrupt = new AtomicBoolean(false);

    private AtomicBoolean cmdCompleted = new AtomicBoolean(false);

    private BlockingQueue<Message> writeQueue = new LinkedBlockingQueue<>(4096);

    public Session(String sessionId, Channel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
    }

    public void clean() {
        cmdCompleted.set(true);
        writeQueue.clear();
        AdviceListenerManager.unReg(sessionId);
    }

    public void reset() {
        cmdCompleted.set(false);
        interrupt.set(false);
        writeQueue.clear();
        AdviceListenerManager.unReg(sessionId);
    }

    public void interrupt() {
        clean();
        channel.writeAndFlush(MessageUtil.buildPrompt(), channel.voidPromise());
    }

    public void destroy() {
        clean();
        DefaultSessionManager.getInstance().remove(sessionId);
    }

    public void sendMessage(Message message) {
        try {
            if (!cmdCompleted.get()) {
                writeQueue.offer(message, 200, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCompleteMessage(Message message) {
        sendMessage(message);
        sendMessage(MessageUtil.buildPrompt());
    }

    public void sendDirectMessage(Message message) {
        channel.writeAndFlush(message, channel.voidPromise());
    }

}
