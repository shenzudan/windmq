package com.stanwind.wmqtt.auth.def;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;

import com.stanwind.wmqtt.auth.SettingFactory;
import com.stanwind.wmqtt.auth.beans.AuthBean;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * DefaultSettingFactory 默认不处理
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 15:44
 **/
public class DefaultSettingFactory implements SettingFactory {

    @Override
    public MqttConnectOptions set(String[] serverURIs, Integer keepAliveInterval, AuthBean authBean) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(serverURIs);
        options.setUserName(authBean.getUsername());
        options.setPassword(authBean.getPassword().toCharArray());
        options.setKeepAliveInterval(keepAliveInterval);
        options.setCleanSession(false);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setAutomaticReconnect(true);
        options.setMqttVersion(MQTT_VERSION_3_1_1);
        options.setConnectionTimeout(5000);

        return options;
    }
}
