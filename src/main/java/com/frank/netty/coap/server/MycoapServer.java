package com.frank.netty.coap.server;

import com.frank.netty.mqtt.server.handler.NettyMqttServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import javax.annotation.PreDestroy;

public class MycoapServer {


    private static final int PORT = 5683;

    private static Channel serverChannel;
    private static NioEventLoopGroup bossGroup;

    public void init() {
        bossGroup = new NioEventLoopGroup();

        final Bootstrap bootstrap = new Bootstrap();

        bootstrap
                .group(bossGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {

                    @Override
                    protected void initChannel(NioDatagramChannel ch){
                        ch.pipeline().addLast(new NettyMqttServerHandler());
                    }
                });

        bind(bootstrap,PORT);
    }

    private static void bind(Bootstrap bootstrap, int port) {
//        try {
//            serverChannel = bootstrap.bind(port).addListener(future -> {
//                if (future.isSuccess()) {
//                    System.out.println(new Date() + ": 端口【" + port + "】 绑定成功");
//                } else {
//                    System.out.println(new Date() + ": 端口【" + port + "】 绑定失败");
//                }
//            }).sync().channel();
//        } catch (InterruptedException e) {
//            System.out.println("服务端报错了---------");
//            e.printStackTrace();
//        }
        try {
            serverChannel =   bootstrap.bind(port).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @PreDestroy
    public void shutdown() throws InterruptedException {
        System.out.println("Stopping MQTT transport!");
        try {
            serverChannel.close().sync();
        } finally {
            bossGroup.shutdownGracefully();
        }
        System.out.println("MQTT transport stopped!");
    }

}
