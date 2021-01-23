package com.stanwind.wmqtt.auth.aliyun;

import com.alibaba.fastjson.JSONObject;
import com.stanwind.wmqtt.auth.ClientApi;
import com.stanwind.wmqtt.auth.beans.AuthBean;
import com.stanwind.wmqtt.auth.beans.ConnData;
import com.stanwind.wmqtt.utils.HttpExecutor;
import com.stanwind.wmqtt.utils.Tools;
import java.io.IOException;
import java.security.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * AliApi api接口实现
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 16:36
 **/
@Slf4j
public class AliApi implements ClientApi {

    /**
     * token服务地址，参考文档获取 https://help.aliyun.com/document_detail/54226.html?spm=a2c4g.11186623.6.559.6ccf5695eZsC77
     */
    private final String TOKEN_SERVER_URL = "https://mqauth.aliyuncs.com";
    private static final String ENDPOINT = "http://{INSTANCE}.mqtt.aliyuncs.com";
    private static final String API_ONLINE_NUM = "/rute/clientId/get";
    private static final String APPLY_TOKEN = "/token/apply";
    private static final String REVOKE_TOKEN = "/token/revoke";

    private static final String ACL_READ = "R";
    private static final String ACL_WRITE = "W";


    @Autowired
    private HttpExecutor httpExecutor;

    @Autowired
    private AuthBean authBean;

    /**
     * 获取请求路径
     */
    protected String getUrl(String path) {
        return ENDPOINT.replace("{INSTANCE}", authBean.getInstanceId()) + path;
    }

    public String queryForOnlineNum(String clientId) throws InvalidKeyException, NoSuchAlgorithmException {
        Map<String, String> params = new HashMap<>();
        params.put("accessKey", authBean.getAccessKey());
        params.put("resource", clientId);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("instanceId", authBean.getInstanceId());
        String signature = Tools.doHttpSignature(params, authBean.getSecretKey());
        params.put("signature", signature);

        return httpExecutor.doGet(getUrl(API_ONLINE_NUM), params);
    }

    public String applyToken(List<String> topics, String action, Long expire) throws InvalidKeyException, NoSuchAlgorithmException {
        Map<String, String> paramMap = new HashMap<>();
        Collections.sort(topics);
        StringBuilder builder = new StringBuilder();
        for (String topic : topics) {
            builder.append(topic).append(",");
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        paramMap.put("resources", builder.toString());
        paramMap.put("actions", action);
        paramMap.put("serviceName", "mq");
        paramMap.put("expireTime", String.valueOf(expire));
        paramMap.put("instanceId", authBean.getInstanceId());
        String signature = Tools.doHttpSignature(paramMap, authBean.getSecretKey());
        paramMap.put("proxyType", "MQTT");
        paramMap.put("accessKey", authBean.getAccessKey());
        paramMap.put("signature", signature);
        JSONObject object = JSONObject.parseObject(httpExecutor.doPost(TOKEN_SERVER_URL + APPLY_TOKEN, paramMap));
        log.info("token授权topic: {} action: {} res: {}", topics.toArray(), action, object);
        if (object != null) {
            return (String) object.get("tokenData");
        }
        return null;
    }

    /**
     * 获取登录数据
     * @param readTopics
     * @param writeTopics
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    @Override
    public ConnData getTokenConn(List<String> readTopics, List<String> writeTopics) throws Exception {
        Long expire = System.currentTimeMillis() + TOKEN_EXPIRE;
        Map<String/*action*/, String/*topics*/> aclMap = new HashMap<>();
        String r = null;
        String w = null;
        if (readTopics != null && !readTopics.isEmpty()) {
            r = applyToken(readTopics, ACL_READ, expire);
            aclMap.put(ACL_READ, r);
        }

        if (writeTopics != null && !writeTopics.isEmpty()) {
            w = applyToken(writeTopics, ACL_WRITE, expire);
            aclMap.put(ACL_WRITE, w);
        }

        String username = "Token|" + authBean.getAccessKey() + "|" + authBean.getInstanceId();
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : aclMap.entrySet()) {
            builder.append(entry.getKey()).append("|").append(entry.getValue()).append("|");
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }

        ConnData data = new ConnData().setUsername(username).setPassword(builder.toString()).setExpire(expire);
        log.info("Ali连接数据: {} \r\n Read:\t{} Token:\t{}\r\nWrite: {} Token: {}", data, readTopics, r, writeTopics, w);

        return data;
    }

    /**
     * 提前注销 token，一般在 token 泄露出现安全问题时，提前禁用特定的客户端
     *
     * @param token 禁用的 token 内容
     */
    public void revokeToken(String token)
            throws InvalidKeyException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("token", token);
        String signature = Tools.doHttpSignature(paramMap, authBean.getSecretKey());
        paramMap.put("signature", signature);
        paramMap.put("accessKey", authBean.getAccessKey());
        JSONObject object = JSONObject.parseObject(httpExecutor.doPost(TOKEN_SERVER_URL + REVOKE_TOKEN, paramMap));
        log.info("回收权限: {}", object);
    }
}
