package com.frank.netty.util;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Created by admin on 2019/3/19.
 */
public class SslUtil {

    public static SSLContext createSSLContext(String type , String path , String password) throws Exception {
        String keypass = "654321";
        String storepass = "123456";
        // "JKS"
        KeyStore ks = KeyStore.getInstance(type);
        // 证书存放地址
        InputStream ksInputStream = new FileInputStream(path);
        ks.load(ksInputStream, storepass.toCharArray());
        //KeyManagerFactory充当基于密钥内容源的密钥管理器的工厂。
        //getDefaultAlgorithm:获取默认的 KeyManagerFactory 算法名称。
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keypass.toCharArray());
        //SSLContext的实例表示安全套接字协议的实现，它充当用于安全套接字工厂或 SSLEngine 的工厂。
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }
}
