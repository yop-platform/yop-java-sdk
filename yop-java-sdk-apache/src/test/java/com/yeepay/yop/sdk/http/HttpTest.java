/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.http.impl.apache.IdleConnectionReaper;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 5/21/21
 */
public class HttpTest {

    private static CloseableHttpClient httpClient;

    private static String basePath = "http://172.18.162.165:8080";
    private static int maxTotal = 5, maxPerRoute = 5, soTimeout = 5000, conTimeout = 1000;

    static {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(soTimeout)
                .setConnectTimeout(conTimeout)
                .setConnectionRequestTimeout(1000)
//                .setStaleConnectionCheckEnabled(true)
                .build();
        HttpClientConnectionManager httpClientConnectionManager = createHttpClientConnectionManager();
        IdleConnectionReaper.registerConnectionManager(httpClientConnectionManager);
        httpClient = HttpClientBuilder.create()
//                .setMaxConnTotal(1)
//                .setMaxConnPerRoute(1)
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(httpClientConnectionManager).disableAutomaticRetries()
                .build();
    }

    private static HttpClientConnectionManager createHttpClientConnectionManager() {
        ConnectionSocketFactory socketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory;
        try {
            SSLContext s = SSLContext.getDefault();
            sslSocketFactory = new SSLConnectionSocketFactory(s,
                    SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            throw new RuntimeException("Fail to create SSLConnectionSocketFactory", e);
        }
        Registry<ConnectionSocketFactory> registry =
                RegistryBuilder.<ConnectionSocketFactory>create().register("http", socketFactory)
                        .register("https", sslSocketFactory).build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager
                .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(soTimeout)
                        .setTcpNoDelay(true).build());
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        connectionManager.setValidateAfterInactivity(3000);
        return connectionManager;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService thread = Executors.newFixedThreadPool(25, new ThreadFactory() {
            int i = 0;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "TestThreadPool-" + (i++));
            }
        });

        for (int i = 0; i < 100; i++) {
            final int suffix = i;
            thread.execute(() -> remoteRequest("子线程(" + suffix + ")", suffix % 100 != 3));
        }

        thread.awaitTermination(10, TimeUnit.MINUTES);
        thread.shutdownNow();

        remoteRequest("主线程", false);
    }

    private static void remoteRequest(String thread, boolean close) {
        System.out.println(thread + "请求执行开始");
        String result = "空";
//        try (CloseableHttpResponse response = httpClient.execute(RequestBuilder.get(basePath + "/test").build())) {
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(RequestBuilder.get(basePath + "/test").build());
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
                throw new RuntimeException("http code:" + statusCode + ", reason:" + statusLine.getReasonPhrase());
            }
            result = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (close) {
                System.out.println(thread + "执行结束，结果：" + result + ", close=true");
                HttpClientUtils.closeQuietly(response);
            } else {
                System.out.println(thread + "执行结束，结果：" + result + ", close=false");
            }
        }
    }
}
