package com.yeepay.yop.sdk.http.impl.apache;

import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.*;
import com.yeepay.yop.sdk.internal.MultiPartFile;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.utils.HttpUtils;
import com.yeepay.yop.sdk.utils.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.yeepay.yop.sdk.YopConstants.*;

/**
 * title: Yop http客户端<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/17 19:22
 */
public class YopHttpClient extends AbstractYopHttpClient {

    /**
     * Logger providing detailed information on requests/responses. Users can enable this logger to get access to YOP
     * request IDs for responses, individual requests and parameters sent to YOP, etc.
     */
    private static final Logger logger = LoggerFactory.getLogger(YopHttpClient.class);

    /**
     * Internal client for sending HTTP requests
     */
    private final CloseableHttpClient httpClient;

    private final HttpClientConnectionManager connectionManager;

    private final RequestConfig defaultConfig;
    private CredentialsProvider credentialsProvider;
    private HttpHost proxyHttpHost;

    private static final DefaultHostnameVerifier HOSTNAME_VERIFIER_INSTANCE = new DefaultHostnameVerifier();

    /**
     * Constructs a new YOP client using the specified client configuration options (ex: max retry attempts, proxy
     * settings, etc), and request metric collector.
     *
     * @param clientConfig Configuration options specifying how this client will communicate with YOP (ex: proxy settings,
     *               retry count, etc.).
     * @throws IllegalArgumentException If config or signer is null.
     */
    public YopHttpClient(ClientConfiguration clientConfig) {
        super(clientConfig);
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setConnectTimeout(clientConfig.getConnectionTimeoutInMillis())
                .setConnectionRequestTimeout(clientConfig.getConnectionRequestTimeoutInMillis())
                .setSocketTimeout(clientConfig.getSocketTimeoutInMillis())
                .setStaleConnectionCheckEnabled(true);
        if (clientConfig.getLocalAddress() != null) {
            requestConfigBuilder.setLocalAddress(clientConfig.getLocalAddress());
        }

        // http 代理
        String proxyHost = clientConfig.getProxyHost();
        int proxyPort = clientConfig.getProxyPort();
        if (proxyHost != null && proxyPort > 0) {
            this.proxyHttpHost = new HttpHost(proxyHost, proxyPort, clientConfig.getProxyScheme());
            requestConfigBuilder.setProxy(this.proxyHttpHost);

            this.credentialsProvider = new BasicCredentialsProvider();
            String proxyUsername = clientConfig.getProxyUsername();
            String proxyPassword = clientConfig.getProxyPassword();
            String proxyDomain = clientConfig.getProxyDomain();
            String proxyWorkstation = clientConfig.getProxyWorkstation();
            if (proxyUsername != null && proxyPassword != null) {
                this.credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
                        new NTCredentials(proxyUsername, proxyPassword,
                                proxyWorkstation, proxyDomain));
            }
        }

        this.connectionManager = this.createHttpClientConnectionManager();
        this.defaultConfig = requestConfigBuilder.build();
        this.httpClient = this.createHttpClient(this.connectionManager, defaultConfig);
        IdleConnectionReaper.registerConnectionManager(this.connectionManager);
    }

    @Override
    protected <Input extends BaseRequest> YopHttpResponse doExecute(Request<Input> request, YopRequestConfig yopRequestConfig) throws IOException {
        return new YopApacheHttpResponse(this.httpClient.execute(createHttpRequest(request), createHttpContext(request, yopRequestConfig)));
    }

    /**
     * Create connection manager for http client.
     *
     * @return The connection manager for http client.
     */
    private HttpClientConnectionManager createHttpClientConnectionManager() {
        LayeredConnectionSocketFactory sslSocketFactory;
        try {
            SSLContext sslContext = getSSLContext();
            sslSocketFactory = new SSLConnectionSocketFactory(sslContext, HOSTNAME_VERIFIER_INSTANCE);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new YopClientException("Fail to create SSLConnectionSocketFactory", e);
        }
        RegistryBuilder registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create().register(Protocol.HTTPS.toString(), sslSocketFactory)
                .register(Protocol.HTTP.toString(), PlainConnectionSocketFactory.getSocketFactory());
        Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager
                .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(this.clientConfig.getSocketTimeoutInMillis())
                        .setTcpNoDelay(true).build());
        connectionManager.setDefaultMaxPerRoute(this.clientConfig.getMaxConnectionsPerRoute());
        connectionManager.setMaxTotal(this.clientConfig.getMaxConnections());
        connectionManager.setValidateAfterInactivity(3000);
        return connectionManager;
    }

    private SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        // 根据JDK版本设置最高TLS版本
        String javaVersion = System.getProperty(JDK_VERSION);
        String tlsVersion = null;
        if (StringUtils.startsWith(javaVersion, JDK_VERSION_1_8) || StringUtils.startsWith(javaVersion, JDK_VERSION_1_7)) {
            tlsVersion = TLS_VERSION_1_2;
        } else if (StringUtils.startsWith(javaVersion, JDK_VERSION_1_6)) {
            tlsVersion = TLS_VERSION_1_1;
        }

        // 加载证书文件
//        KeyManager[] kms = null;
//        String keyStoreFileName = "";
//        char[] keyStorePwd = "".toCharArray();
//
//        try {
//            KeyStore ks = KeyStore.getInstance("JKS");
//            ks.load(new FileInputStream(keyStoreFileName), keyStorePwd);
//
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//            kmf.init(ks, keyStorePwd);
//            kms = kmf.getKeyManagers();
//        } catch (Exception e) {
//            logger.error("Load KeyStore fail, keyStoreFileName:" + keyStoreFileName, e);
//        }
//
//        TrustManager[] tms = null;
//        String trustKeyStoreFileName = "config/certs/openapi_chain_rsa.jks";
//        char[] trustKeyStorePwd = "xDf2e-Ex3Kl-0iuoS-56Msn".toCharArray();
//
//        InputStream is = null;
//        try {
//            is = FileUtils.getResourceAsStream(trustKeyStoreFileName);
//            KeyStore tks = KeyStore.getInstance("JKS");
//            tks.load(is, trustKeyStorePwd);
//
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//            tmf.init(tks);
//            tms = tmf.getTrustManagers();
//        } catch (Exception e) {
//            logger.error("Load TrustKeyStore fail, trustKeyStoreFileName:" + trustKeyStoreFileName, e);
//        } finally {
//            if (null != is) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        SSLContext s;
        if (StringUtils.isNotEmpty(tlsVersion)) {
            s = SSLContext.getInstance(tlsVersion);
            // 初始化SSLContext实例
            s.init(null, null, RandomUtils.secureRandom());
        } else {
            s = SSLContext.getDefault();
        }
        return s;
    }

    /**
     * Create http client based on connection manager.
     *
     * @param connectionManager The connection manager setting http client.
     * @return Http client based on connection manager.
     */
    private CloseableHttpClient createHttpClient(HttpClientConnectionManager connectionManager,
                                                 RequestConfig requestConfig) {
        HttpClientBuilder builder =
                HttpClients.custom().setConnectionManager(connectionManager).disableAutomaticRetries();

        int socketBufferSizeInBytes = this.clientConfig.getSocketBufferSizeInBytes();
        if (socketBufferSizeInBytes > 0) {
            builder.setDefaultConnectionConfig(
                    ConnectionConfig.custom().setBufferSize(socketBufferSizeInBytes).build());
        }

        return builder.setDefaultRequestConfig(requestConfig).build();
    }

    /**
     * Creates HttpClient Context object based on the internal request.
     *
     * @param request The internal request.
     * @return HttpClient Context object.
     */
    protected HttpClientContext createHttpContext(Request<? extends BaseRequest> request,
                                                  YopRequestConfig yopRequestConfig) {
        HttpClientContext context = HttpClientContext.create();

        // 定制请求级参数
        if (yopRequestConfig.getConnectTimeout() > 0 || yopRequestConfig.getReadTimeout() > 0) {
            RequestConfig.Builder requestConfigBuilder = RequestConfig.copy(this.defaultConfig);
            if (yopRequestConfig.getConnectTimeout() > 0) {
                requestConfigBuilder.setConnectTimeout(yopRequestConfig.getConnectTimeout());
            }
            if (yopRequestConfig.getReadTimeout() > 0) {
                requestConfigBuilder.setSocketTimeout(yopRequestConfig.getReadTimeout());
            }
            context.setRequestConfig(requestConfigBuilder.build());
        }

        if (this.credentialsProvider != null) {
            context.setCredentialsProvider(this.credentialsProvider);
        }
        if (this.clientConfig.isProxyPreemptiveAuthenticationEnabled()) {
            AuthCache authCache = new BasicAuthCache();
            authCache.put(this.proxyHttpHost, new BasicScheme());
            context.setAuthCache(authCache);
        }
        return context;
    }

    private HttpRequestBase createHttpRequest(Request<?> request) {
        HttpRequestBase httpRequest;
        String uri = HttpUtils.appendUri(request.getEndpoint(), request.getResourcePath()).toASCIIString();
        boolean isMultiPart = checkForMultiPart(request);
        if (isMultiPart) {
            httpRequest = buildMultiPartRequest(uri, request);
        } else {
            String encodedParams = HttpUtils.encodeParameters(request, false);

            boolean requestHasPayload = request.getContent() != null;
            boolean requestIsPost = request.getHttpMethod() == HttpMethodName.POST;
            boolean putParamsInUri = !requestIsPost || requestHasPayload;
            if (encodedParams != null && putParamsInUri) {
                uri += "?" + encodedParams;
            }
            long contentLength = -1;
            String contentLengthString = request.getHeaders().get(Headers.CONTENT_LENGTH);
            if (contentLengthString != null) {
                contentLength = Long.parseLong(contentLengthString);
            }
            if (request.getHttpMethod() == HttpMethodName.GET) {
                httpRequest = new HttpGet(uri);
            } else if (request.getHttpMethod() == HttpMethodName.PUT) {
                HttpPut putMethod = new HttpPut(uri);
                httpRequest = putMethod;
                if (request.getContent() != null) {
                    putMethod.setEntity(new InputStreamEntity(request.getContent(), contentLength));
                }
            } else if (request.getHttpMethod() == HttpMethodName.POST) {
                HttpPost postMethod = new HttpPost(uri);
                httpRequest = postMethod;
                if (request.getContent() != null) {
                    postMethod.setEntity(new InputStreamEntity(request.getContent(), contentLength));
                } else if (encodedParams != null) {
                    postMethod.setEntity(new StringEntity(encodedParams, Charset.defaultCharset()));
                }
            } else if (request.getHttpMethod() == HttpMethodName.DELETE) {
                httpRequest = new HttpDelete(uri);
            } else if (request.getHttpMethod() == HttpMethodName.HEAD) {
                httpRequest = new HttpHead(uri);
            } else {
                throw new YopClientException("Unknown HTTP method name: " + request.getHttpMethod());
            }
        }

        // headers
        buildHttpHeaders(request, new ApacheHeaderBuilder(httpRequest));
        if (!isMultiPart) {
            checkNotNull(httpRequest.getFirstHeader(Headers.CONTENT_TYPE), Headers.CONTENT_TYPE + " not set");
        }
        return httpRequest;
    }

    private HttpRequestBase buildMultiPartRequest(String uri, Request<?> request) {
        HttpPost postMethod = new HttpPost(uri);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (Map.Entry<String, List<String>> entry : request.getParameters().entrySet()) {
            String name = entry.getKey();
            for (String value : entry.getValue()) {
                builder.addTextBody(HttpUtils.normalize(name), HttpUtils.normalize(value));
            }
        }
        for (Map.Entry<String, List<MultiPartFile>> entry : request.getMultiPartFiles().entrySet()) {
            String name = entry.getKey();
            for (MultiPartFile multiPartFile : entry.getValue()) {
                builder.addBinaryBody(name, multiPartFile.getInputStream(), ContentType.DEFAULT_BINARY,
                        multiPartFile.getFileName());
            }
        }
        postMethod.setEntity(builder.build());
        return postMethod;
    }

    /**
     * Get delay time before next retry.
     *
     * @param method      The current HTTP method being executed.
     * @param exception   The client/service exception from the failed request.
     * @param attempt     The number of times the current request has been attempted.
     * @param retryPolicy The retryPolicy being used.
     * @return The deley time before next retry.
     */
    protected long getDelayBeforeNextRetryInMillis(HttpRequestBase method, YopClientException exception, int attempt,
                                                   RetryPolicy retryPolicy) {
        int retries = attempt - 1;

        int maxErrorRetry = retryPolicy.getMaxErrorRetry();

        // Immediately fails when it has exceeds the max retry count.
        if (retries >= maxErrorRetry) {
            return -1;
        }

        // Never retry on requests containing non-repeatable entity
        if (method instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) method).getEntity();
            if (entity != null && !entity.isRepeatable()) {
                logger.debug("Entity not repeatable, stop retrying");
                return -1;
            }
        }

        return Math.min(retryPolicy.getMaxDelayInMillis(),
                retryPolicy.getDelayBeforeNextRetryInMillis(exception, retries));
    }

    @Override
    public void shutdown() {
        IdleConnectionReaper.removeConnectionManager(this.connectionManager);
        this.connectionManager.shutdown();
    }

    static class ApacheHeaderBuilder implements HeaderBuilder {

        private final HttpRequestBase httpRequest;

        public ApacheHeaderBuilder(HttpRequestBase httpRequest) {
            this.httpRequest = httpRequest;
        }

        @Override
        public void addHeader(String key, String value) {
            httpRequest.addHeader(key, value);
        }
    }

}
