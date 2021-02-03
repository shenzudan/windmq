package com.stanwind.wmqtt.utils;


import static com.stanwind.wmqtt.beans.Constant.DEVICE_ID_T;
import static com.stanwind.wmqtt.beans.Constant.INSTANCE_ID_T;

import com.stanwind.wmqtt.MqttConfig;
import com.stanwind.wmqtt.handler.MqttContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BaseTopicHandler
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-12 16:36
 **/
public abstract class BaseTopicHandler {

    @Autowired
    private STCUtil stcUtil;

    @Autowired
    private MqttConfig mqttConfig;

    /**
     * sn转换工具
     */
    public String getSn(String clientId) {
        return stcUtil.cli2sn(clientId);
    }


    /**
     * 仅可获取topic中匹配的参数
     *
     * @param name
     */
    public String getParam(String name) {
        if (MqttContext.getContext().getParams() != null) {
            return MqttContext.getContext().getParams().getOrDefault(name, null);
        }

        return null;
    }

    /**
     * 仅可获取topic中匹配的deviceId
     */
    public String getSn() {
        return getSn(getParam(DEVICE_ID_T));
    }


    /**
     * 仅可获取topic中匹配的instanceId是否是当前实例
     */
    public boolean currentHandle() {
        String instanceId = getParam(INSTANCE_ID_T);
        if (instanceId != null) {
            //存在instanceId参数
            return mqttConfig.getInstanceId().equals(instanceId);
        }

        return true;
    }
}
