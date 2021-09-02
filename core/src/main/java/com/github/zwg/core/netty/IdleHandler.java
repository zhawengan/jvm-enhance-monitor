package com.github.zwg.core.netty;

import com.github.zwg.core.session.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleUserEventChannelHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/1
 */
public class IdleHandler extends SimpleUserEventChannelHandler<IdleStateEvent> {

    private final SessionManager sessionManager;

    public IdleHandler(SessionManager sessionManager){

        this.sessionManager = sessionManager;
    }

    @Override
    protected void eventReceived(ChannelHandlerContext ctx,
            IdleStateEvent idleStateEvent) throws Exception {
        if(idleStateEvent.state()== IdleState.READER_IDLE){
            System.out.println("receive reader idle");
            Channel channel = ctx.channel();
            sessionManager.remove(channel);
            channel.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        sessionManager.remove(channel);
        channel.close();
    }
}
