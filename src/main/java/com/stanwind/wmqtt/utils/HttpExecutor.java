package com.stanwind.wmqtt.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * HttpExecutor
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 16:42
 **/
@Component
public class HttpExecutor {

    private static final Logger log = LoggerFactory.getLogger(HttpExecutor.class);

    protected CloseableHttpClient httpClient;

    protected PoolingHttpClientConnectionManager manager;

    /**
     * 定义超时时间
     */
    public int getTimeout() {
        return 3000;
    }

    /**
     * 定义连接数
     */
    public int getConnectionAmount() {
        return 200;
    }

    @PostConstruct
    protected void initHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        //        //HTTPS
        SSLContext ctx = SSLContext.getInstance("TLS");
//         debug 信任所有
//        SSLContext ctx = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();

        X509TrustManager tm = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        ctx.init(null, new TrustManager[]{tm}, null);
        //SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
//        DEBUG
//        SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
        SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(ctx,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", ssf)
                .register("http", PlainConnectionSocketFactory.INSTANCE).build();

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
//                .setProxy(new HttpHost("127.0.0.1", 8888, "http"));//                 debug

        manager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        //连接池的相关配置
        SocketConfig config = SocketConfig.custom().setSoTimeout(getTimeout()).
                setSoKeepAlive(true).setTcpNoDelay(true).build();
        manager.setDefaultSocketConfig(config);
        //控制最大连接数
        manager.setDefaultMaxPerRoute(getConnectionAmount());
        manager.setMaxTotal(getConnectionAmount());
        //为连接池配置管链接理器
        httpClientBuilder.setConnectionManager(manager);
        httpClientBuilder.setDefaultSocketConfig(config);

        //生成我们配置好的httpclient类
        httpClient = httpClientBuilder.build();
    }

    /**
     * 参数转换
     *
     * @param params
     */
    protected static List<NameValuePair> convertParams(Map<String, ? extends Object> params) {
        List<NameValuePair> p = new LinkedList<>();
        params.entrySet().forEach(e -> {
            p.add(new BasicNameValuePair(e.getKey(), String.valueOf(e.getValue())));
        });

        return p;
    }

    public String doPost(String path, Map<String, ?> params) {
        CloseableHttpResponse httpResponse = null;
        try {
            HttpPost httpPost = new HttpPost(path);
            List<NameValuePair> p = convertParams(params);
            httpPost.setEntity(new UrlEncodedFormEntity(p, "utf-8"));
            httpResponse = httpClient.execute(httpPost);
            int code = httpResponse.getStatusLine().getStatusCode();
            String content = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
            log.debug("响应结果[{}]: {}", code, content);

            return code == HttpStatus.SC_OK ? content : null;
        } catch (UnsupportedEncodingException e) {
            log.error("参数转换异常", e);
        } catch (ClientProtocolException e) {
            log.error("请求协议异常", e);
        } catch (IOException e) {
            log.error("请求IO异常", e);
        }

        return null;
    }

    public String doGet(String url, Map<String, String> params) {
        CloseableHttpResponse httpResponse = null;
        try {
            List<NameValuePair> p = convertParams(params);
            String paramUrl = URLEncodedUtils.format(p, Charset.forName("UTF-8"));
            HttpGet request = new HttpGet(url + "?" + paramUrl);
            httpResponse = httpClient.execute(request);
            int code = httpResponse.getStatusLine().getStatusCode();
            String content = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
            log.debug("响应结果[{}]: {}", code, content);

            return code == HttpStatus.SC_OK ? content : null;
        } catch (UnsupportedEncodingException e) {
            log.error("参数转换异常", e);
        } catch (ClientProtocolException e) {
            log.error("请求协议异常", e);
        } catch (IOException e) {
            log.error("请求IO异常", e);
        }

        return null;
    }
}
