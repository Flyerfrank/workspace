package com.frank.netty.mqtt.store;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.io.Serializable;

/**
 * @author frank
 * 订阅存储实体类
 */
public class SubscribeStore implements Serializable {
    private static final long serialVersionUID = 1276156087085594264L;

    private String clientId;

    private String topicFilter;

    private MqttQoS mqttQoS;

    public SubscribeStore(String clientId, String topicFilter, MqttQoS mqttQoS) {
        this.clientId = clientId;
        this.topicFilter = topicFilter;
        this.mqttQoS = mqttQoS;
    }

    public String getClientId() {
        return clientId;
    }

    public SubscribeStore setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    public SubscribeStore setTopicFilter(String topicFilter) {
        this.topicFilter = topicFilter;
        return this;
    }

    public MqttQoS getMqttQoS() {
        return mqttQoS;
    }

    public SubscribeStore setMqttQoS(MqttQoS mqttQoS) {
        this.mqttQoS = mqttQoS;
        return this;
    }
}
