package com.frank.netty.mqtt.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;

public interface MqttHandlerService {


    void doConnectMessage(ChannelHandlerContext ctx, MqttConnectMessage msg);

    void doPublishMessage(ChannelHandlerContext ctx, MqttPublishMessage msg);

    void doSubMessage(ChannelHandlerContext ctx, MqttSubscribeMessage msg);

    void doPingreoMessage(ChannelHandlerContext ctx, MqttMessage msg);

    void doPingrespMessage(ChannelHandlerContext ctx, MqttMessage msg);

    void doUnSubscribe(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg);
}
