package com.github.zwg.core.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.io.PrintWriter;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/1
 */
public class ConnClient {

    public Channel conn(String host,int port, PrintWriter writer,String sessionId) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,Boolean.TRUE)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(30,0,0));
                            //处理粘包和拆包问题
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(20*1024,0,4,0,4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            //自定义协议编解码
                            pipeline.addLast(new MessageEncoder());
                            pipeline.addLast(new MessageDecoder());
                            //具体消息处理器
                            pipeline.addLast(new ClientMessageHandler(sessionId,writer));
                        }
                    });
            Channel channel = bootstrap.connect(host, port).sync().channel();
            register(sessionId, channel);
            return channel;
        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
            System.exit(0);
        }
        return null;
    }

    private void register(String sessionId,Channel channel){
        Message message = MessageUtil.buildRegister(sessionId, "I'm a jem client");
        channel.writeAndFlush(message,channel.voidPromise());
    }

}
