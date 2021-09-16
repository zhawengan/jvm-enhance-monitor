package com.github.zwg.core.netty;

import com.github.zwg.core.command.CommandFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class ConnServer {

    private final Instrumentation inst;

    public ConnServer(Instrumentation inst) {

        this.inst = inst;
    }

    public void start(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //初始化
            CommandFactory.getInstance();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //心跳
                            pipeline.addLast(new IdleHandler());
                            pipeline.addLast(new IdleStateHandler(30, 0, 0));
                            //处理粘包和拆包问题
                            pipeline.addLast(
                                    new LengthFieldBasedFrameDecoder(20 * 1024, 0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            //自定义协议编解码
                            pipeline.addLast(new MessageEncoder());
                            pipeline.addLast(new MessageDecoder());
                            //具体消息处理器
                            pipeline.addLast(new ServerMessageHandler(inst));
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ConnServer(null).start(8080);
    }


    public static Collection<Class<?>> getLoadClasses() {
        try {
            ClassLoader classLoader = ConnServer.class.getClassLoader();
            Field field = ClassLoader.class.getDeclaredField("classes");
            field.setAccessible(true);
            Collection<Class<?>> data = (Collection<Class<?>>) field.get(classLoader);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
