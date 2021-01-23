package com.stanwind.wmqtt.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class Tools {
    public static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ClassLoader.getSystemResourceAsStream("test.properties"));
        } catch (IOException e) {
        }
        return properties;
    }

    /**
     * 计算签名，参数分别是参数对以及密钥
     *
     * @param requestParams 参数对，即参与计算签名的参数
     * @param secretKey 密钥
     * @return 签名字符串
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String doHttpSignature(Map<String, String> requestParams,
        String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        List<String> paramList = new ArrayList<String>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            paramList.add(entry.getKey() + "=" + entry.getValue());
        }
        Collections.sort(paramList);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < paramList.size(); i++) {
            if (i > 0) {
                sb.append('&');
            }
            sb.append(paramList.get(i));
        }
        return macSignature(sb.toString(), secretKey);
    }

    /**
     * @param text 要签名的文本
     * @param secretKey 阿里云MQ secretKey
     * @return 加密后的字符串
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public static String macSignature(String text,
        String secretKey) throws InvalidKeyException, NoSuchAlgorithmException {
        Charset charset = Charset.forName("UTF-8");
        String algorithm = "HmacSHA1";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(secretKey.getBytes(charset), algorithm));
        byte[] bytes = mac.doFinal(text.getBytes(charset));
        return new String(Base64.encodeBase64(bytes), charset);
    }

    /**
     * 去除最后一个/后面的字符串
     * @param topic
     * @return
     */
    public static String fuzzyTopic(String topic) {
        if (topic == null || topic.length() == 0) {
            return topic;
        }

        int index = topic.lastIndexOf("/");

        return index == -1 ? topic : topic.substring(0, index);
    }


    public static void main(String[] args) {
        System.out.println(fuzzyTopic("IOT_CLIENT/setting/ANNDSSSJJJNN"));
        System.out.println(fuzzyTopic("IOT_CLIENT"));
        System.out.println(fuzzyTopic(""));
        System.out.println(fuzzyTopic(""));
    }
}
