package com.frank.netty.service;


public interface AuthService {

    /**
     * 验证用户名和密码是否正确
     */
    boolean checkValid(String username, String password);
}
