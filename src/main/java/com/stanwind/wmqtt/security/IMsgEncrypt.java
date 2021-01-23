package com.stanwind.wmqtt.security;

import org.springframework.messaging.Message;

/**
 * IMsgEncrypt 加密
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-20 16:14
 **/
public interface IMsgEncrypt {

    /**
     * 消息是否需要加解密
     * @param message
     * @return
     */
    boolean isEncrypt(Message<?> message);

    /**
     * 消息是否需要加解密
     * @param topic
     * @return
     */
    boolean isEncrypt(String topic);

    /**
     * 消息体加密
     * @param data
     * @return
     */
    byte[] doEncrypt(byte[] data);

    /**
     * 消息体解密
     * @param data
     * @return
     */
    byte[] doDecrypt(byte[] data);
}
