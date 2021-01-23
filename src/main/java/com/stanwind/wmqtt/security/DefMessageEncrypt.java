package com.stanwind.wmqtt.security;

import org.springframework.messaging.Message;

/**
 * DefMessageEncrypt 不加密
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-20 16:44
 **/
public class DefMessageEncrypt implements IMsgEncrypt {

    @Override
    public boolean isEncrypt(String topic) {
        return false;
    }

    @Override
    public boolean isEncrypt(Message<?> message) {
        return false;
    }

    @Override
    public byte[] doEncrypt(byte[] data) {
        return data;
    }

    @Override
    public byte[] doDecrypt(byte[] data) {
        return data;
    }
}
