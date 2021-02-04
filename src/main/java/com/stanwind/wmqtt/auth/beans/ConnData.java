package com.stanwind.wmqtt.auth.beans;

import java.io.Serializable;

/**
 * Conndata mqtt连接数据
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-20 14:40
 **/
public class ConnData implements Serializable {
    private Long expire;
    private String username;
    private String password;

    public Long getExpire() {
        return expire;
    }

    public ConnData setExpire(Long expire) {
        this.expire = expire;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public ConnData setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ConnData setPassword(String password) {
        this.password = password;
        return this;
    }
}
