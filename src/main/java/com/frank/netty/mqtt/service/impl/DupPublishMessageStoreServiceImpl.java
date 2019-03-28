package com.frank.netty.mqtt.service.impl;

import com.frank.netty.mqtt.service.DupPublishMessageStoreService;
import com.frank.netty.mqtt.store.DupPublishMessageStore;

import java.util.List;

public class DupPublishMessageStoreServiceImpl implements DupPublishMessageStoreService {
    @Override
    public void put(String clientId, DupPublishMessageStore dupPublishMessageStore) {

    }

    @Override
    public List<DupPublishMessageStore> get(String clientId) {
        return null;
    }

    @Override
    public void remove(String clientId, int messageId) {

    }

    @Override
    public void removeByClient(String clientId) {

    }
}
