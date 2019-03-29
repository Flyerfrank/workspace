package com.frank.netty;

import com.frank.netty.server.NettyMqttServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class NettyApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(NettyApplication.class, args);
        NettyMqttServer bean = context.getBean(NettyMqttServer.class);
        bean.init();
    }
}
