package com.frank.netty.mqtt.client.handler;

import com.frank.netty.mqtt.domain.MqttOpntions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class NettyMqttClientHandler extends SimpleChannelInboundHandler<MqttMessage> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端===channelActive");
        MqttOpntions mqtt = new MqttOpntions();

        mqtt.setHasCleanSession(true);
        mqtt.setHasWillFlag(true);
        mqtt.setClientIdentifier("client_123456");
        mqtt.setWillTopic("test_1");
        mqtt.setWillQos(1);
        mqtt.setHasWillRetain(false);
        mqtt.setWillMessage("message=======");
        mqtt.setHasUserName(false);
        mqtt.setHasPassword(false);

        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                MqttMessageType.CONNECT,
                false,
                MqttQoS.AT_LEAST_ONCE,
                false,
                10);

        MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(
                MqttVersion.MQTT_3_1_1.protocolName(),
                MqttVersion.MQTT_3_1_1.protocolLevel(),
                mqtt.isHasUserName(),
                mqtt.isHasPassword(),
                mqtt.isHasWillRetain(),
                mqtt.getWillQos(),
                mqtt.isHasWillFlag(),
                mqtt.isHasCleanSession(),
                mqtt.getKeepAliveTime());


        MqttConnectPayload mqttConnectPayload = new MqttConnectPayload(
                mqtt.getClientIdentifier(),
                mqtt.getWillTopic(),
                mqtt.getWillMessage(),
                mqtt.getUserName(),
                mqtt.getPassword());
        MqttConnectMessage mqttConnectMessage = new MqttConnectMessage(mqttFixedHeader,mqttConnectVariableHeader,mqttConnectPayload);

        ctx.channel().writeAndFlush(mqttConnectMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            ctx.close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage mqttMessage) throws Exception {
        doMessage(ctx, mqttMessage);
    }


    private void doMessage(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttFixedHeader fixedHeader = mqttMessage.fixedHeader();
        MqttMessageType mqttMessageType = fixedHeader.messageType();
        switch (mqttMessageType){
            case UNSUBACK:  //取消订阅确认消息
                System.out.println("我是UNSUBACK");
//                mqttHandlerApi.unsubBack(channelHandlerContext.channel(),mqttMessage);
                break;
            case CONNACK:   //连接确认消息
                System.out.println("我是CONNACK");
                connectBack(ctx,(MqttConnAckMessage) mqttMessage);
                break;
            case PUBLISH:
                System.out.println("我是PUBLISH");
                publish(ctx,(MqttPublishMessage)mqttMessage);
                break;
            case PUBACK: // qos 1回复确认
                System.out.println("我是PUBACK");
//                mqttHandlerApi.puback(channelHandlerContext.channel(),mqttMessage);
                break;
            case PUBREC: //发布已接收
                System.out.println("我是PUBREC");
//                mqttHandlerApi.pubrec(channelHandlerContext.channel(),mqttMessage);
                break;
            case PUBREL: //发布释放
                System.out.println("我是PUBREL");
//                mqttHandlerApi.pubrel(channelHandlerContext.channel(),mqttMessage);
                break;
            case PUBCOMP: //发布完成
                System.out.println("我是PUBCOMP");
//                mqttHandlerApi.pubcomp(channelHandlerContext.channel(),mqttMessage);
                break;
            case SUBACK: //确认订阅
                System.out.println("我是SUBACK");
                suback(ctx,(MqttSubAckMessage)mqttMessage);
                break;
            default:
                break;
        }
//        System.out.println("收到服务端的msg = " + mqttMessage);




    }

    /**
     * 收到服务端发来的消息
     * @param ctx
     * @param mqttMessage
     */
    private void publish(ChannelHandlerContext ctx, MqttPublishMessage mqttMessage) {
        MqttFixedHeader mqttFixedHeader = mqttMessage.fixedHeader();
        MqttPublishVariableHeader mqttPublishVariableHeader = mqttMessage.variableHeader();
//        ByteBuf payload = mqttMessage.payload();
//        byte[] bytes = new byte[payload.readableBytes()];
//        payload.readBytes(bytes);
        ByteBuf buf = mqttMessage.payload();
        String msg = new String(ByteBufUtil.getBytes(buf));
//        String msg = new String(ByteBufUtil.getBytes(payload));

        System.out.println("客户端收到服务端推送消息------topic="+mqttPublishVariableHeader.topicName()+"消息----"+msg);
        switch (mqttFixedHeader.qosLevel()){
            case AT_MOST_ONCE:
                break;
            case AT_LEAST_ONCE:
                pubBackMessage(ctx,mqttPublishVariableHeader.messageId());
                break;
            case EXACTLY_ONCE:
                pubRecMessage(ctx,mqttPublishVariableHeader.messageId());
                break;
        }

    }

    private void pubRecMessage(ChannelHandlerContext ctx, int messageId) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC,false, MqttQoS.AT_LEAST_ONCE,false,0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        MqttMessage mqttPubAckMessage = new MqttMessage(mqttFixedHeader,from);
        ctx.channel().writeAndFlush(mqttPubAckMessage);
    }

    private void pubBackMessage(ChannelHandlerContext ctx, int messageId) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK,false, MqttQoS.AT_LEAST_ONCE,false,0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(mqttFixedHeader,from);
        ctx.channel().writeAndFlush(mqttPubAckMessage);
    }

    /**
     * 订阅确认
     * @param ctx
     * @param mqttMessage
     */
    private void suback(ChannelHandlerContext ctx, MqttSubAckMessage mqttMessage) {
//        ScheduledFuture<?> scheduledFuture = ctx.channel().attr(getKey(Integer.toString(mqttMessage.variableHeader().messageId()))).get();
//        if(scheduledFuture!=null){
//            scheduledFuture.cancel(true);
//        }

//        MqttSubAckPayload payload = mqttMessage.payload();
//        ByteBuf buf = mqttPublishMessage.payload();
//        String msg = new String(ByteBufUtil.getBytes(buf));

//        System.out.println("msg = " + msg);
        System.out.println("订阅确认消息——MqttSubAckMessage："+ mqttMessage);
    }

    private  static final CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 连接确认
     */
    private void connectBack(ChannelHandlerContext ctx, MqttConnAckMessage mqttMessage) {
        System.out.println("连接确认消息——MqttConnAckMessage："+ mqttMessage);
        MqttConnAckVariableHeader mqttConnAckVariableHeader = mqttMessage.variableHeader();
        switch ( mqttConnAckVariableHeader.connectReturnCode()){
            case CONNECTION_ACCEPTED:
                countDownLatch.countDown();
                break;
            case CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD:
//                log.error("login error", new RuntimeException("用户名密码错误"));
                break;
            case CONNECTION_REFUSED_IDENTIFIER_REJECTED:
//                log.error("login error", new RuntimeException("clientId  不允许链接"));
                break;
            case CONNECTION_REFUSED_SERVER_UNAVAILABLE:
//                log.error("login error",  new RuntimeException("服务不可用"));
                break;
            case CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION:
//                log.error("login error",  new RuntimeException("mqtt 版本不可用"));
                break;
            case CONNECTION_REFUSED_NOT_AUTHORIZED:
//                log.error("login error", new RuntimeException("未授权登录"));
                break;
        }


        /**
         * 订阅消息
         */
        sub(ctx,mqttMessage);

    }

    /**
     * 订阅消息
     */
    private void sub(ChannelHandlerContext ctx, MqttConnAckMessage mqttMessage) {
        //固定报文头
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                MqttMessageType.SUBSCRIBE,
                false,
                MqttQoS.AT_LEAST_ONCE,
                false,
                10);

        //可变报文头
        int messageid = 1;
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(messageid);

        //订阅的主题
        List<MqttTopicSubscription> topicSubscriptions = new ArrayList<>();
        MqttTopicSubscription mqttTopicSubscription = new MqttTopicSubscription("test_1",MqttQoS.AT_LEAST_ONCE);
        topicSubscriptions.add(mqttTopicSubscription);


        //消息体
        MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(topicSubscriptions);

        //订阅消息
        MqttSubscribeMessage mqttSubscribeMessage = new MqttSubscribeMessage(mqttFixedHeader,mqttMessageIdVariableHeader,mqttSubscribePayload);

        ctx.channel().writeAndFlush(mqttSubscribeMessage);
    }

}
