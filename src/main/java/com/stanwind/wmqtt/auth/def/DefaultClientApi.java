package com.stanwind.wmqtt.auth.def;

import com.stanwind.wmqtt.auth.ClientApi;
import com.stanwind.wmqtt.auth.beans.AuthBean;
import com.stanwind.wmqtt.auth.beans.ConnData;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * DefaultClientApi
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-20 15:38
 **/
public class DefaultClientApi implements ClientApi {

    private static final Logger log = LoggerFactory.getLogger(DefaultClientApi.class);

    @Autowired
    private AuthBean authBean;

    @Override
    public ConnData getTokenConn(List<String> readTopics, List<String> writeTopics) throws Exception {
        Long expire = System.currentTimeMillis() + TOKEN_EXPIRE;
        //直接返回账号密码
        return new ConnData().setUsername(authBean.getUsername()).setPassword(authBean.getPassword()).setExpire(expire);
    }
}
