package com.stanwind.wmqtt.auth;

import com.stanwind.wmqtt.auth.beans.AuthBean;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * SettingFactory 鉴权数据生成
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 15:40
 **/
public interface SettingFactory {
    MqttConnectOptions set(String[] serverURIs, Integer keepAliveInterval, AuthBean authBean) throws Exception;
}
