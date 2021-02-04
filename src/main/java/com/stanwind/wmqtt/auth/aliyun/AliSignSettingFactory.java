package com.stanwind.wmqtt.auth.aliyun;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;

import com.stanwind.wmqtt.auth.SettingFactory;
import com.stanwind.wmqtt.auth.beans.AuthBean;
import com.stanwind.wmqtt.utils.Tools;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * AliSignSettingFactory 阿里云签名模式MQTT
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 15:58
 **/
public class AliSignSettingFactory implements SettingFactory {

    /**
     *
     * @param serverURIs
     * @param keepAliveInterval
     * @param authBean
     * @return
     * @throws Exception
     */
    @Override
    public MqttConnectOptions set(String[] serverURIs, Integer keepAliveInterval, AuthBean authBean)
            throws Exception {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(serverURIs);
        options.setUserName("Signature|" + authBean.getAccessKey() + "|" + authBean.getInstanceId());
        options.setPassword(Tools.macSignature(authBean.getClientId(), authBean.getSecretKey()).toCharArray());
        options.setCleanSession(false);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setAutomaticReconnect(true);
        options.setMqttVersion(MQTT_VERSION_3_1_1);
        options.setConnectionTimeout(5000);

        return options;
    }
}
