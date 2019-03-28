package com.frank.netty.mqtt.server;

import com.frank.netty.mqtt.server.handler.NettyMqttServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.Date;

@Configuration
public class NettyMqttServer {

    private static final int PORT = 8000;

    private static Channel serverChannel;
    private static NioEventLoopGroup bossGroup;
    private static NioEventLoopGroup workerGroup;



    public void init() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        final ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap
                .group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .option(ChannelOption.TCP_NODELAY,true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch){
                        ch.pipeline().addLast(new MqttDecoder());
                        ch.pipeline().addLast(new NettyMqttServerHandler());
                        ch.pipeline().addLast(MqttEncoder.INSTANCE);
                    }
                });

        bind(serverBootstrap,PORT);
    }

    private static void bind(ServerBootstrap serverBootstrap, int port) {
        try {
            serverChannel = serverBootstrap.bind(port).addListener(future -> {
                 if (future.isSuccess()) {
                     System.out.println(new Date() + ": 端口【" + port + "】 绑定成功");
                 } else {
                     System.out.println(new Date() + ": 端口【" + port + "】 绑定失败");
                 }
             }).sync().channel();
        } catch (InterruptedException e) {
            System.out.println("服务端报错了---------");
            e.printStackTrace();
        }
    }


    @PreDestroy
    public void shutdown() throws InterruptedException {
        System.out.println("Stopping MQTT transport!");
//        log.info("Stopping MQTT transport!");
        try {
            serverChannel.close().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
        System.out.println("MQTT transport stopped!");
//        log.info("MQTT transport stopped!");
    }
}
