package com.frank.netty.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.frank.netty.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.security.interfaces.RSAPrivateKey;


@Service
public class AuthServiceImpl implements AuthService {
    private RSAPrivateKey privateKey;

    @Override
    public boolean checkValid(String username, String password) {
        if (StringUtils.isEmpty(username)){
            return false;
        }
        if (StringUtils.isEmpty(password)){
            return false;
        }
//        RSA rsa = new RSA(privateKey,null);
//        String value = rsa.encryptBcd(username, KeyType.PrivateKey);
//        return value.equals(password) ? true : false;
        return true;
    }

//    @PostConstruct
//    public void init() {
//        privateKey = IoUtil.readObj(this.getClass().getClassLoader().getResourceAsStream("keystore/auth-private.key"));
//    }
}
