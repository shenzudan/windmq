package com.stanwind.wmqtt.auth.beans;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Conndata
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-20 14:40
 **/
@Data
@Accessors(chain = true)
public class ConnData implements Serializable {
    private Long expire;
    private String username;
    private String password;
}
