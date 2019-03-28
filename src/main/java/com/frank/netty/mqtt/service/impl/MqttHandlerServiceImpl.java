package com.frank.netty.mqtt.service.impl;

import com.frank.netty.mqtt.service.MqttHandlerService;
import com.frank.netty.mqtt.service.SessionStoreService;
import com.frank.netty.mqtt.service.SubscribeStoreService;
import com.frank.netty.mqtt.store.SessionStore;
import com.frank.netty.mqtt.store.SubscribeStore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MqttHandlerServiceImpl implements MqttHandlerService {

    @Autowired
    private SubscribeStoreService subscribeStoreService;
    @Autowired
    private SessionStoreService sessionStoreService;

   private MqttHandlerServiceImpl(){}

   private static class MqttHandlerInstance{
       private static final MqttHandlerServiceImpl INSTANCE = new MqttHandlerServiceImpl();
   }

   public static MqttHandlerServiceImpl getInstance(){
       return MqttHandlerInstance.INSTANCE;
   }

    @Override
    public void doConnectMessage(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        // 消息解码器出现异常
        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                // 不支持的协议版本
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
                ctx.channel().writeAndFlush(connAckMessage);
                ctx.channel().close();
                return;
            } else if (cause instanceof MqttIdentifierRejectedException) {
                // 不合格的clientId
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
                ctx.channel().writeAndFlush(connAckMessage);
                ctx.channel().close();
                return;
            }
            ctx.channel().close();
            return;
        }

        // clientId为空或null的情况, 这里要求客户端必须提供clientId, 不管cleanSession是否为1, 此处没有参考标准协议实现
        if (StringUtils.isEmpty(msg.payload().clientIdentifier())) {
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
            ctx.channel().writeAndFlush(connAckMessage);
            ctx.channel().close();
            return;
        }

        //用户名密码验证

        //处理遗嘱信息,当启用遗嘱消息功能时，服务端先存储遗嘱消息,当服务端认为连接异常时，即可发布遗嘱消息
        SessionStore sessionStore = new SessionStore(msg.payload().clientIdentifier(), ctx.channel(), msg.variableHeader().isCleanSession(), null);
        if (msg.variableHeader().isWillFlag()){
            MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH,false, MqttQoS.valueOf(msg.variableHeader().willQos()),msg.variableHeader().isWillRetain(),0),
                    new MqttPublishVariableHeader(msg.payload().willTopic(),0),
                    Unpooled.buffer().writeBytes(msg.payload().willMessage().getBytes())
            );
            sessionStore.setWillMessage(willMessage);
        }



        //存储会话消息及返回接受客户端连接
//        sessionStoreService.put(msg.payload().clientIdentifier(),sessionStore);

//        Map<String,SessionStore> map = new HashMap<>();
//        map.put(msg.payload().clientIdentifier(),sessionStore);
        sessionStoreService.put(msg.payload().clientIdentifier(),sessionStore);

        //将clientId存储到channel的map中
        ctx.channel().attr(AttributeKey.valueOf("clientId")).set(msg.payload().clientIdentifier());
        Boolean sessionPresent = sessionStoreService.containsKey(msg.payload().clientIdentifier()) && !msg.variableHeader().isCleanSession();
        MqttConnAckMessage connAck = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK,false,MqttQoS.AT_MOST_ONCE,false,0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED,sessionPresent),
                null
        );
        ctx.channel().writeAndFlush(connAck);
        System.out.println("连接消息 msg = " + msg);




//
//        MqttFixedHeader mqttFixedHeader1 = msg.fixedHeader();
//
//        MqttConnectReturnCode connectReturnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;
//
//        MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(connectReturnCode, false);
//
//        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
//                MqttMessageType.CONNACK,
//                mqttFixedHeader1.isDup(),
//                MqttQoS.AT_MOST_ONCE,
//                mqttFixedHeader1.isRetain(),
//                0x02);
//
//        MqttConnAckMessage connAck = new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
//
//        ctx.channel().attr(AttributeKey.valueOf("clientId")).set(msg.payload().clientIdentifier());
//
//        ctx.channel().writeAndFlush(connAck);
    }

    @Override
    public void doPublishMessage(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        ByteBuf buf = msg.payload();
        String msg1 = new String(ByteBufUtil.getBytes(buf));
        System.out.println("msg1 = " + msg1);
//        msg.payload().

        String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("clientId")).get();
        // QoS=0
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_MOST_ONCE) {
            byte[] messageBytes = new byte[msg.payload().readableBytes()];
            msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
//            InternalMessage internalMessage = new InternalMessage()
//                    .setTopic(msg.variableHeader().topicName())
//                    .setMqttQoS(msg.fixedHeader().qosLevel().value())
//                    .setMessageBytes(messageBytes)
//                    .setDup(false)
//                    .setRetain(false)
//                    .setClientId(clientId);

//            grozaKafkaService.send(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
        }
        // QoS=1
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            byte[] messageBytes = new byte[msg.payload().readableBytes()];
            msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
//            InternalMessage internalMessage = new InternalMessage()
//                    .setTopic(msg.variableHeader().topicName())
//                    .setMqttQoS(msg.fixedHeader().qosLevel().value())
//                    .setMessageBytes(messageBytes)
//                    .setDup(false)
//                    .setRetain(false)
//                    .setClientId(clientId);
//            grozaKafkaService.send(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
            this.sendPubBack(ctx, msg.variableHeader().messageId());
        }
        // QoS=2
        if (msg.fixedHeader().qosLevel() == MqttQoS.EXACTLY_ONCE) {
            byte[] messageBytes = new byte[msg.payload().readableBytes()];
            msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
//            InternalMessage internalMessage = new InternalMessage()
//                    .setTopic(msg.variableHeader().topicName())
//                    .setMqttQoS(msg.fixedHeader().qosLevel().value())
//                    .setMessageBytes(messageBytes)
//                    .setDup(false)
//                    .setRetain(false)
//                    .setClientId(clientId);
//            grozaKafkaService.send(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
            this.sendPubRec(ctx, msg.variableHeader().messageId());
        }



//        ByteBuf buf = mqttPublishMessage.payload();
//        String msg = new String(ByteBufUtil.getBytes(buf));
//
//        System.out.println("收到客户端消息msg = " + msg);
//        MqttFixedHeader publishfixedHeader = mqttPublishMessage.fixedHeader();
//
//        //收到客户端订阅消息的可变报文头
//        MqttPublishVariableHeader mqttPublishVariableHeader = mqttPublishMessage.variableHeader();
//
//        //消息id
//        int messageId = mqttPublishVariableHeader.messageId();
//        if (messageId == -1)
//            messageId = 1;
//
//
//        switch (publishfixedHeader.qosLevel()) {
//            case AT_MOST_ONCE: // 至多一次
//                break;
//            case AT_LEAST_ONCE: //最少一次
//                sendPubBack(ctx, messageId);
//                break;
//            case EXACTLY_ONCE: //仅此一次
//                sendPubRec(ctx, messageId);
//                break;
//        }



    }

    /**
     * 发布消息
     * @param topic
     * @param mqttQoS
     * @param messageBytes
     * @param retain
     * @param dup
     */
    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        List<SubscribeStore> subscribeStores = subscribeStoreService.search(topic);

        subscribeStores.forEach(subscribeStore -> {

            if (sessionStoreService.containsKey(subscribeStore.getClientId())) {
                // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
                MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS().value() ? MqttQoS.valueOf(subscribeStore.getMqttQoS().value()) : mqttQoS;
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, 0),
                            Unpooled.buffer().writeBytes(messageBytes));
//                    log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
                    sessionStoreService.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    System.out.println("至少一次。。。。");
//                    int messageId = grozaMessageIdService.getNextMessageId();
//                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
//                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
//                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
////                    log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
//                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(subscribeStore.getClientId())
//                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes).setMessageId(messageId);
//                    grozaDupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
//                    sessionStoreService.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    System.out.println("仅此一次。。。。");
//                    int messageId = grozaMessageIdService.getNextMessageId();
//                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
//                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
//                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
////                    log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
//                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(subscribeStore.getClientId())
//                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes).setMessageId(messageId);
//                    grozaDupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
//                    sessionStoreService.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
            }


        });
    }


    @Override
    public void doSubMessage(ChannelHandlerContext ctx, MqttSubscribeMessage mqttSubscribeMessage) {
        Set<String> topics = mqttSubscribeMessage.payload().topicSubscriptions().stream().map(mqttTopicSubscription ->
                mqttTopicSubscription.topicName()
        ).collect(Collectors.toSet());
//        mqttChannelService.suscribeSuccess(mqttChannelService.getDeviceId(channel), topics);
        subBack(ctx, mqttSubscribeMessage, topics.size());
    }

    @Override
    public void doPingreoMessage(ChannelHandlerContext ctx, MqttMessage msg) {
        MqttMessage pingRespMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0),
                null,
                null);
//        log.info("PINGREQ - clientId: {}", (String) ctx.channel().attr(AttributeKey.valueOf("clientId")).get());
        ctx.channel().writeAndFlush(pingRespMessage);
    }

    @Override
    public void doPingrespMessage(ChannelHandlerContext ctx, MqttMessage msg) {

    }

    /**
     * 发送qos1 publish  确认消息
     */
    private void sendPubBack(ChannelHandlerContext ctx, int messageId) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                MqttMessageType.PUBACK,
                false,
                MqttQoS.AT_MOST_ONCE,
                false,
                0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(mqttFixedHeader,from);
        ctx.channel().writeAndFlush(mqttPubAckMessage);
    }

    /**
     * Qos 2时，需要三步确认
     *
     * 发送qos2 publish  确认消息 第一步
     */
    private void sendPubRec(ChannelHandlerContext ctx, int messageId) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC,false, MqttQoS.AT_LEAST_ONCE,false,0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(mqttFixedHeader,from);
        Channel channel = ctx.channel();
        channel.writeAndFlush(mqttPubAckMessage);
//        SendMqttMessage sendMqttMessage = addQueue(channel, messageId, null, null, null, ConfirmStatus.PUBREC);
//        mqttChannel.addSendMqttMessage(messageId,sendMqttMessage);
    }

    /**
     * 发送qos2 publish  确认消息 第二步
     */
    protected void sendPubRel(Channel channel,boolean isDup,int messageId){
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL,isDup, MqttQoS.AT_LEAST_ONCE,false,0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(mqttFixedHeader,from);
        channel.writeAndFlush(mqttPubAckMessage);
    }

    /**
     * 发送qos2 publish  确认消息 第三步
     */
    protected void sendToPubComp(Channel channel,int messageId){
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP,false, MqttQoS.AT_MOST_ONCE,false,0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(mqttFixedHeader,from);
        channel.writeAndFlush(mqttPubAckMessage);
    }

    /**
     * 收到 MqttSubscribeMessage
     * @param ctx
     * @param mqttSubscribeMessage
     * @param num
     */
    private void subBack(ChannelHandlerContext ctx, MqttSubscribeMessage mqttSubscribeMessage, int num) {

        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                MqttMessageType.SUBACK,
                false,
                MqttQoS.AT_MOST_ONCE,
                false,
                0);

        MqttMessageIdVariableHeader variableHeader =
                MqttMessageIdVariableHeader.from(mqttSubscribeMessage.variableHeader().messageId());

        List<Integer> grantedQoSLevels = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            grantedQoSLevels.add(mqttSubscribeMessage.payload().topicSubscriptions().get(i).qualityOfService().value());
        }

        MqttSubAckPayload payload = new MqttSubAckPayload(grantedQoSLevels);
        MqttSubAckMessage mqttSubAckMessage = new MqttSubAckMessage(mqttFixedHeader, variableHeader, payload);
        ctx.channel().writeAndFlush(mqttSubAckMessage);
    }
}
