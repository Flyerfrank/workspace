package com.frank.netty.web;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPrivateKey;

/**
 * 根据用户名获取密码
 */
@RestController
@RequestMapping("/zydl")
@Slf4j
public class
AuthApiController {

    @RequestMapping(value = "/{username}/auth",method = RequestMethod.GET,produces = "application/json")
    public String getPwd(@PathVariable("username") String username){
        RSAPrivateKey privateKey = IoUtil.readObj(AuthApiController.class.getClassLoader().getResourceAsStream("keystore/auth-private.key"));
        RSA rsa = new RSA(privateKey, null);
        return rsa.encryptBcd(username, KeyType.PrivateKey);
    }


}
