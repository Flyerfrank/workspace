package com.frank.netty.mqtt.client;

import com.frank.netty.ssl.SecureSokcetTrustManagerFactory;
import com.frank.netty.mqtt.client.handler.NettyMqttClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.ssl.SslHandler;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NettyMqttClient {
    private static final int MAX_RETRY = 5;
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;


    private static Channel clientChannel;
    private static NioEventLoopGroup workerGroup;

    private static  SSLContext clientContext;

    private static final String PROTOCOL = "TLS";

    public static void main(String[] args) throws Exception{

         workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        bootstrap
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>()  {

                    @Override
                    protected void initChannel(SocketChannel ch)throws Exception{

////                        String path = ClassLoader.getSystemResource("testwss.jks").getPath();
//                        SSLContext sslContext = SslUtil.createSSLContext("JKS","D:\\javaTools\\Java\\jdk1.8.0_161\\bin\\testwss.jks","123456");
//                        //SSLEngine 此类允许使用ssl安全套接层协议进行安全通信
//                        SSLEngine engine = sslContext.createSSLEngine();
//                        engine.setUseClientMode(false);
////                        ch.pipeline().addLast("sslHandler", new SslHandler(HttpSslContextFactory.createSSLEngine()));
//                        ch.pipeline().addLast(new SslHandler(engine));
//
//                        SSLEngine engine =
//                                SERVER_CONTEXT.createSSLEngine();
//                        engine.setUseClientMode(false);
//                        ch.pipeline().addLast("ssl", new SslHandler(engine));
                        initSsl();
                        SSLEngine engine =
                                clientContext.createSSLEngine();
                        engine.setUseClientMode(true);
                        ch.pipeline().addLast("ssl", new SslHandler(engine));
                        ch.pipeline().addLast(new MqttDecoder());
                        ch.pipeline().addLast(new NettyMqttClientHandler());
                        ch.pipeline().addLast(MqttEncoder.INSTANCE);
                    }
                });

        connect(bootstrap,HOST, PORT,MAX_RETRY);
    }

    private static void initSsl() {
        try {
            clientContext = SSLContext.getInstance(PROTOCOL);
            clientContext.init(null, SecureSokcetTrustManagerFactory.getTrustManagers(), null);
        } catch (Exception e) {
            throw new Error(
                    "Failed to initialize the client-side SSLContext", e);
        }
    }

    private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
        try {
            clientChannel = bootstrap.connect(host, port).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println(new Date() + ": 连接成功");
    //                Channel channel = ((ChannelFuture) future).channel();
    //                startConsoleThread(channel);
                } else if (retry == 0) {
                    System.out.println("重连次数已用完，放弃连接！");
                } else {
                    //第几次重连
                    int order = (MAX_RETRY - retry) + 1;

                    //本次重连间隔
                    int delay = 1 << retry;

                    System.err.println(new Date() + ": 连接失败，第" + order + "次重连......");

                    bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
                }
            }).sync().channel();
        } catch (InterruptedException e) {
            System.out.println("报错了------------");
            e.printStackTrace();
        }
//        bootstrap.connect(host,port).addListener(future -> {
//           if (future.isSuccess()){
//               Channel channel = ((ChannelFuture) future).channel();
//               //连接成功后启动控制台线程
//               startConsoleThread(channel);
//           }
//        });
    }

//    private static void startConsoleThread(Channel channel) {
//        new Thread(() -> {
//            while (!Thread.interrupted()) {
//                if (LoginUtil.hasLogin(channel)) {
//                    System.out.println("输入消息发送至服务端: ");
//                    Scanner sc = new Scanner(System.in);
//                    String line = sc.nextLine();
//
//                    MessageRequestPacket packet = new MessageRequestPacket();
//                    packet.setMessage(line);
////                    ByteBuf byteBuf = PacketCodeC.INSTANCE.encode(channel.alloc(), packet);
////                    channel.writeAndFlush(byteBuf);
//                }
//            }
//        }).start();
//    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
//        log.info("Stopping MQTT transport!");
        try {
            clientChannel.close().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
//        log.info("MQTT transport stopped!");
    }
}
