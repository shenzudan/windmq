package com.stanwind.wmqtt.auth.beans;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AuthBean 鉴权基础数据包装
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 15:42
 **/
@Data
@Accessors(chain = true)
public class AuthBean implements Serializable {
    private String instanceId;
    private String accessKey;
    private String secretKey;

    private String clientId;
    private String username;
    private String password;
}
