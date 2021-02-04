package com.stanwind.wmqtt.handler;

import static com.stanwind.wmqtt.beans.Constant.CHANNEL_NAME_OUT;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * MsgSendHandler
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-10 19:54
 **/
@Component
@MessagingGateway(defaultRequestChannel = CHANNEL_NAME_OUT)
public interface MqttSender {
    /**
     * 发送信息到MQTT服务器
     *
     * @param data 发送的文本
     */
    void send(Object data);

    /**
     * 发送信息到MQTT服务器
     *
     * @param topic 主题
     * @param payload 消息主体
     */
//    @Gateway(replyTimeout = 2, requestTimeout = 1000)
    void send(@Header(MqttHeaders.TOPIC) String topic,
            Object payload);

    /**
     * 发送信息到MQTT服务器
     *
     * @param topic 主题
     * @param qos 对消息处理的几种机制。
     * 0 表示的是订阅者没收到消息不会再次发送，消息会丢失。
     * 1 表示的是会尝试重试，一直到接收到消息，但这种情况可能导致订阅者收到多次重复消息。
     * 2 多了一次去重的动作，确保订阅者收到的消息有一次。
     * @param payload 消息主体
     */
    void send(@Header(MqttHeaders.TOPIC) String topic,
            @Header(MqttHeaders.QOS) int qos,
            Object payload);
}
