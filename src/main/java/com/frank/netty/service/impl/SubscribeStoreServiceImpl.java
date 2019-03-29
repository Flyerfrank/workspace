package com.frank.netty.service.impl;

import com.frank.netty.service.SubscribeStoreService;
import com.frank.netty.store.SubscribeStore;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SubscribeStoreServiceImpl implements SubscribeStoreService {


    private Map<String, List<SubscribeStore>> subscribeCache = new ConcurrentHashMap<String, List<SubscribeStore>>();


    @Override
    public void put(String topicFilter, SubscribeStore subscribeStore) {
        List<SubscribeStore> subscribeStores = new ArrayList<>();
        subscribeStores.add(subscribeStore);
        subscribeCache.put(topicFilter,subscribeStores);
    }

    @Override
    public void remove(String topicFilter, String clientId) {
        List<SubscribeStore> subscribeStores = search(topicFilter);
        subscribeStores.stream().filter(subscribeStore ->
                subscribeStore.getClientId().equals(clientId) && subscribeStore.getTopicFilter().equals(topicFilter)
        ).forEach(subscribeStore -> {
            subscribeCache.remove(topicFilter);
        });
    }

    @Override
    public void removeForClient(String clientId) {

    }

    @Override
    public List<SubscribeStore> search(String topic) {
        return subscribeCache.get(topic);
    }
}
