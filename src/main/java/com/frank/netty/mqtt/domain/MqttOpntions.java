package com.frank.netty.mqtt.domain;

import lombok.Data;

@Data
public  class MqttOpntions {

    private  String clientIdentifier;

    private  String willTopic;

    private  String willMessage;

    private  String userName;

    private  String password;

    private  boolean hasUserName;

    private  boolean hasPassword;

    private  boolean hasWillRetain;

    private  int willQos;

    private  boolean hasWillFlag;

    private  boolean hasCleanSession;

    private int KeepAliveTime;


}