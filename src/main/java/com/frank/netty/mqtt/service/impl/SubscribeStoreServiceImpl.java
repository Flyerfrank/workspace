package com.frank.netty.mqtt.service.impl;

import com.frank.netty.mqtt.service.SubscribeStoreService;
import com.frank.netty.mqtt.store.SubscribeStore;
import io.netty.handler.codec.mqtt.MqttQoS;

import java.util.ArrayList;
import java.util.List;


public class SubscribeStoreServiceImpl implements SubscribeStoreService {

    @Override
    public void put(String topicFilter, SubscribeStore subscribeStore) {

    }

    @Override
    public void remove(String topicFilter, String clientId) {

    }

    @Override
    public void removeForClient(String clientId) {

    }

    @Override
    public List<SubscribeStore> search(String topic) {
        List<SubscribeStore> list = new ArrayList<>();
        list.add(new SubscribeStore("client_123456","test_1", MqttQoS.AT_MOST_ONCE));
        return list;
    }
}
