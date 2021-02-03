package com.stanwind.wmqtt.auth.beans;

import java.io.Serializable;

/**
 * AuthBean 鉴权基础数据包装
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 15:42
 **/
public class AuthBean implements Serializable {
    private String instanceId;
    private String accessKey;
    private String secretKey;

    private String clientId;
    private String username;
    private String password;

    public String getInstanceId() {
        return instanceId;
    }

    public AuthBean setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public AuthBean setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public AuthBean setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public AuthBean setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AuthBean setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AuthBean setPassword(String password) {
        this.password = password;
        return this;
    }
}
