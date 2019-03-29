package com.frank.netty.server;

import com.frank.netty.server.handler.NettyMqttServerHandler;
import com.frank.netty.util.SslUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.ssl.SslHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.security.KeyStore;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class NettyMqttServer {

    private static final int PORT = 8000;

    private static Channel serverChannel;
    private static NioEventLoopGroup bossGroup;
    private static NioEventLoopGroup workerGroup;


    @Autowired
    private NettyMqttServerHandler nettyMqttServerHandler;

    private String protocal = "TLS";

    private SSLContext sslContext;

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
                        //（服务端加载证书，客户端将服务端的证书加载到客户端信任库中）自认为，不确定是否是这样
                        initSsl();
                        SSLEngine engine =  sslContext.createSSLEngine();
                        //服务端模式
                        engine.setUseClientMode(false);
                        //不需要验证客户端
                        engine.setNeedClientAuth(false);
                        ch.pipeline().addLast("ssl", new SslHandler(engine));
                        ch.pipeline().addLast(new MqttDecoder());
                        ch.pipeline().addLast(nettyMqttServerHandler);
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
    public void shutdown(){
        System.out.println("Stopping MQTT transport!");
//        log.info("Stopping MQTT transport!");
        try {
            serverChannel.close().sync();
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
        System.out.println("MQTT transport stopped!");
//        log.info("MQTT transport stopped!");
    }

    //加载证书
    private void initSsl(){
        try {
            sslContext = SslUtil.createSSLContext("JKS", ClassLoader.getSystemResource("secure.jks").getPath(), "123456");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
