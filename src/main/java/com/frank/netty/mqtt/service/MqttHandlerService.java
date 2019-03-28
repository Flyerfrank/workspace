package com.frank.netty.mqtt.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;

public interface MqttHandlerService {


    void doConnectMessage(ChannelHandlerContext ctx, MqttConnectMessage msg);

    void doPublishMessage(ChannelHandlerContext ctx, MqttPublishMessage msg);

    void doSubMessage(ChannelHandlerContext ctx, MqttSubscribeMessage msg);

    void doPingreoMessage(ChannelHandlerContext ctx, MqttMessage msg);

    void doPingrespMessage(ChannelHandlerContext ctx, MqttMessage msg);

}
