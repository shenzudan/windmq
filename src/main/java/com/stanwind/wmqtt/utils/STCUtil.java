package com.stanwind.wmqtt.utils;

import com.stanwind.wmqtt.MqttConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * STCUtil sn to client sn 和 clientId互转
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-12 19:43
 **/
@Component
public class STCUtil {

    @Autowired
    private MqttConfig config;

    /**
     * clientId转序列号
     * @param clientId 按照规则会以"GID_DEVICE@@@@"开头
     * @return
     */
    public String cli2sn(String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            return clientId;
        }

        return clientId.startsWith(config.getClientIdPrefix())
                ? clientId.replace(config.getClientIdPrefix(), "")
                : clientId;
    }

    /**
     * 序列号转clientId
     * @param sn 按照规则会以"GID_DEVICE@@@@"开头
     * @return
     */
    public String sn2cli(String sn) {
        if (sn == null) {
            return sn;
        }

        return config.getClientIdPrefix() + sn;
    }
}
