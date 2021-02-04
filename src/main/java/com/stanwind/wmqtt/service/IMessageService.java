package com.stanwind.wmqtt.service;

import com.stanwind.wmqtt.beans.MqttRequest;
import com.stanwind.wmqtt.beans.MqttResponse;
import org.springframework.messaging.Message;

/**
 * IMessageService
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-10 19:53
 **/
public interface IMessageService {

    /**
     * 发送消息给device
     * @param deviceId
     * @param payload
     */
    void notify(String deviceId, Object payload);

    /**
     * 发送消息到topic
     * @param topic
     * @param payload
     */
    void notifyToTopic(String topic, Object payload);

    /**
     * 同步确认发送消息给设备
     * @param deviceId
     * @param payload
     * @return
     */
    MqttResponse request(String deviceId, MqttRequest payload);

    /**
     * 同步确认发送消息给设备
     * @param deviceId
     * @param payload
     * @param timeout 等待超时 ms
     * @return
     */
    MqttResponse request(String deviceId, MqttRequest payload, long timeout);

    /**
     * 响应同步消息
     * @param message
     * @return
     */
    boolean response(Message<MqttResponse> message);

    /**
     * 客户端请求通用回复
     * @param messageId
     * @param deviceId
     * @param result
     */
    void sendCommonResponse(Long messageId, String deviceId, Integer result);

    /**
     * 客户端请求通用回复
     * @param messageId
     * @param deviceId
     */
    void sendCommonResponse(Long messageId, String deviceId);
}
