package com.frank.netty.service.impl;

import com.frank.netty.service.SessionStoreService;
import com.frank.netty.store.SessionStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话存储接口类
 */
public class SessionStoreServiceImpl implements SessionStoreService {

    private Map<String, SessionStore> sessionCache = new ConcurrentHashMap<String, SessionStore>();

    @Override
    public void put(String clientId, SessionStore sessionStore) {
        sessionCache.put(clientId,sessionStore);
    }

    @Override
    public SessionStore get(String clientId) {
        return sessionCache.get(clientId);
    }

    @Override
    public boolean containsKey(String clientId) {
        return sessionCache.containsKey(clientId);
    }

    @Override
    public void remove(String clientId) {
        sessionCache.remove(clientId);
    }
}
