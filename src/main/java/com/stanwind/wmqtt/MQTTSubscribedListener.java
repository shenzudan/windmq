package com.stanwind.wmqtt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.mqtt.event.MqttSubscribedEvent;
import org.springframework.stereotype.Component;

/**
 * MQTTSubscribedListener 绑定监听器
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-16 17:50
 **/
@Slf4j
@Component
public class MQTTSubscribedListener implements ApplicationListener<MqttSubscribedEvent> {

    @Override
    public void onApplicationEvent(MqttSubscribedEvent event) {
        log.debug("Subscribed Success: " + event.getMessage());
    }
}
