package com.stanwind.wmqtt;

import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;

/**
 * Windmq producer holder
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2021-02-03 12:00 PM
 **/
public class ProducerHolder {
    public final MqttPahoMessageDrivenChannelAdapter adapter;

    public ProducerHolder(MqttPahoMessageDrivenChannelAdapter adapter) {
        this.adapter = adapter;
    }

    public MqttPahoMessageDrivenChannelAdapter getAdapter() {
        return adapter;
    }

    /**
     * 订阅topic
     * @param topic
     */
    public void addTopic(String... topic) {
        adapter.addTopic(topic);
    }

    /**
     * 订阅topic
     * @param topic
     * @param qos
     */
    public void addTopic(String topic, int qos) {
        adapter.addTopic(topic, qos);
    }

    /**
     * 订阅topic
     * @param topic
     * @param qos
     */
    public void addTopics(String[] topic, int[] qos) {
        adapter.addTopics(topic, qos);
    }

    /**
     * 移除订阅topic
     * @param topic
     */
    public void removeTopic(String... topic) {
        adapter.removeTopic(topic);
    }
}
