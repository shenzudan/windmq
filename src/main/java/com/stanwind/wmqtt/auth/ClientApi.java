package com.stanwind.wmqtt.auth;

import com.stanwind.wmqtt.auth.beans.ConnData;
import java.util.List;

/**
 * ClientApi
 * 客户端获取链接
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-20 15:36
 **/
public interface ClientApi {
    /**
     * 12小时 过期
     */
    Long TOKEN_EXPIRE = 12 * 3600 * 1000L;
    /**
     * 获取登录信息
     * @param readTopics 可读topics
     * @param writeTopics 可写topics
     * @return
     * @throws Exception
     */
    ConnData getTokenConn(List<String> readTopics, List<String> writeTopics) throws Exception;
}
