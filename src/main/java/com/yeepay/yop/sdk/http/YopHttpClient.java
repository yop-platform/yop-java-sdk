package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.internal.MultiPartFile;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.RequestConfig;
import com.yeepay.yop.sdk.utils.HttpUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
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
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HttpContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

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
public class YopHttpClient {

    /**
     * Logger providing detailed information on requests/responses. Users can enable this logger to get access to YOP
     * request IDs for responses, individual requests and parameters sent to YOP, etc.
     */
    private static final Logger requestLogger = LoggerFactory.getLogger("com.yeepay.g3.yop.sdk.request");

    /**
     * Logger for more detailed debugging information, that might not be as useful for end users (ex: HTTP client
     * configuration, etc).
     */
    private static final Logger logger = LoggerFactory.getLogger(YopHttpClient.class);

    /**
     * Internal client for sending HTTP requests
     */
    private final CloseableHttpClient httpClient;

    /**
     * Client configuration options, such as proxy settings, max retries, etc.
     */
    private final ClientConfiguration config;

    private final HttpClientConnectionManager connectionManager;

    private final org.apache.http.client.config.RequestConfig.Builder requestConfigBuilder;
    private CredentialsProvider credentialsProvider;
    private HttpHost proxyHttpHost;

    static {
        Security.removeProvider("SunEC");
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Constructs a new YOP client using the specified client configuration options (ex: max retry attempts, proxy
     * settings, etc), and request metric collector.
     *
     * @param config Configuration options specifying how this client will communicate with YOP (ex: proxy settings,
     *               retry count, etc.).
     * @throws IllegalArgumentException If config or signer is null.
     */
    public YopHttpClient(ClientConfiguration config) {
        checkNotNull(config, "config should not be null.");
        this.config = config;
        this.connectionManager = this.createHttpClientConnectionManager();
        this.httpClient = this.createHttpClient(this.connectionManager);
        IdleConnectionReaper.registerConnectionManager(this.connectionManager);

        this.requestConfigBuilder = org.apache.http.client.config.RequestConfig.custom();
        this.requestConfigBuilder.setConnectTimeout(config.getConnectionTimeoutInMillis());
        this.requestConfigBuilder.setStaleConnectionCheckEnabled(true);
        if (config.getLocalAddress() != null) {
            this.requestConfigBuilder.setLocalAddress(config.getLocalAddress());
        }

        String proxyHost = config.getProxyHost();
        int proxyPort = config.getProxyPort();
        if (proxyHost != null && proxyPort > 0) {
            this.proxyHttpHost = new HttpHost(proxyHost, proxyPort, config.getProxyScheme());
            this.requestConfigBuilder.setProxy(this.proxyHttpHost);

            this.credentialsProvider = new BasicCredentialsProvider();
            String proxyUsername = config.getProxyUsername();
            String proxyPassword = config.getProxyPassword();
            String proxyDomain = config.getProxyDomain();
            String proxyWorkstation = config.getProxyWorkstation();
            if (proxyUsername != null && proxyPassword != null) {
                this.credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
                        new NTCredentials(proxyUsername, proxyPassword,
                                proxyWorkstation, proxyDomain));
            }
        }
    }

    public <Output extends BaseResponse, Input extends BaseRequest> Output execute(Request<Input> request,
                                                                                   RequestConfig requestConfig,
                                                                                   ExecutionContext executionContext,
                                                                                   HttpResponseHandler<Output> responseHandler) {
        YopCredentials yopCredentials = executionContext.getYopCredentials();
        setAppKey(request, yopCredentials);
        setUserAgent(request);
        HttpRequestBase httpRequest;
        CloseableHttpResponse httpResponse;
        CloseableHttpAsyncClient httpAsyncClient = null;
        try {
            signRequest(request, executionContext);
            if (BooleanUtils.isTrue(requestConfig.getNeedEncrypt())) {
                encryptRequest(request, executionContext);
            }
            requestLogger.debug("Sending Request: {}", request);
            httpRequest = this.createHttpRequest(request);
            HttpContext httpContext = this.createHttpContext(request);
            if (this.config.isHttpAsyncPutEnabled() && httpRequest.getMethod().equals("PUT")) {
                httpAsyncClient = this.createHttpAsyncClient(this.createNHttpClientConnectionManager());
                httpAsyncClient.start();
                Future<HttpResponse> future = httpAsyncClient.execute(HttpAsyncMethods.create(httpRequest),
                        new BasicAsyncResponseConsumer(),
                        httpContext, null);
                httpResponse = new YopCloseableHttpResponse(future.get());
            } else {
                httpResponse = this.httpClient.execute(httpRequest, httpContext);
            }
            HttpUtils.printRequest(httpRequest);
            return responseHandler.handle(new HttpResponseHandleContext(httpResponse, request, requestConfig, executionContext));
        } catch (Exception e) {
            YopClientException yop;
            if (e instanceof YopClientException) {
                yop = (YopClientException) e;
            } else {
                yop = new YopClientException("Unable to execute HTTP request", e);
            }
            throw yop;
        } finally {
            try {
                if (httpAsyncClient != null) {
                    httpAsyncClient.close();
                }
            } catch (IOException e) {
                logger.debug("Fail to close HttpAsyncClient", e);
            }
        }
    }

    /**
     * 加密请求
     *
     * @param request          请求
     * @param executionContext 执行上下文
     * @param <Input>          请求类
     */
    private <Input extends BaseRequest> void encryptRequest(Request<Input> request, ExecutionContext executionContext) {
        executionContext.getEncryptor().encrypt(request);
    }

    private <Input extends BaseRequest> void signRequest(Request<Input> request, ExecutionContext executionContext) {
        executionContext.getSigner().sign(request, executionContext.getYopCredentials(), executionContext.getSignOptions());
    }

    /**
     * Create connection manager for http client.
     *
     * @return The connection manager for http client.
     */
    private HttpClientConnectionManager createHttpClientConnectionManager() {
        ConnectionSocketFactory socketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory;
        try {
            String javaVersion = System.getProperty("java.version");
            String tlsVersion = null;
            if (StringUtils.startsWith(javaVersion, "1.8") || StringUtils.startsWith(javaVersion, "1.7")) {
                tlsVersion = "TLSv1.2";
            } else if (StringUtils.startsWith(javaVersion, "1.6")) {
                tlsVersion = "TLSv1.1";
            }
            SSLContext s;
            if (StringUtils.isNotEmpty(tlsVersion)) {
                s = SSLContext.getInstance(tlsVersion);
                // 初始化SSLContext实例
                s.init(null, null, null);
            } else {
                s = SSLContext.getDefault();
            }
            sslSocketFactory = new SSLConnectionSocketFactory(s,
                    SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new YopClientException("Fail to create SSLConnectionSocketFactory", e);
        }
        Registry<ConnectionSocketFactory> registry =
                RegistryBuilder.<ConnectionSocketFactory>create().register(Protocol.HTTP.toString(), socketFactory)
                        .register(Protocol.HTTPS.toString(), sslSocketFactory).build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager
                .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(this.config.getSocketTimeoutInMillis())
                        .setTcpNoDelay(true).build());
        connectionManager.setMaxTotal(this.config.getMaxConnections());
        return connectionManager;
    }

    /**
     * Create connection manager for asynchronous http client.
     *
     * @return Connection manager for asynchronous http client.
     * @throws IOReactorException in case if a non-recoverable I/O error.
     */
    protected NHttpClientConnectionManager createNHttpClientConnectionManager() throws IOReactorException, NoSuchAlgorithmException, KeyManagementException {
        ConnectingIOReactor ioReactor =
                new DefaultConnectingIOReactor(IOReactorConfig.custom()
                        .setSoTimeout(this.config.getSocketTimeoutInMillis()).setTcpNoDelay(true).build());
        SSLContext s = SSLContext.getInstance("TLSv1.2");
        // 初始化SSLContext实例
        s.init(null, null, null);
        Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder.<SchemeIOSessionStrategy>create()
                .register("https", new SSLIOSessionStrategy(s))
                .build();
        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor, sessionStrategyRegistry);
        connectionManager.setDefaultMaxPerRoute(this.config.getMaxConnections());
        connectionManager.setMaxTotal(this.config.getMaxConnections());
        return connectionManager;
    }

    /**
     * Create http client based on connection manager.
     *
     * @param connectionManager The connection manager setting http client.
     * @return Http client based on connection manager.
     */
    private CloseableHttpClient createHttpClient(HttpClientConnectionManager connectionManager) {
        HttpClientBuilder builder =
                HttpClients.custom().setConnectionManager(connectionManager).disableAutomaticRetries();

        int socketBufferSizeInBytes = this.config.getSocketBufferSizeInBytes();
        if (socketBufferSizeInBytes > 0) {
            builder.setDefaultConnectionConfig(
                    ConnectionConfig.custom().setBufferSize(socketBufferSizeInBytes).build());
        }

        return builder.build();
    }

    /**
     * Create asynchronous http client based on connection manager.
     *
     * @param connectionManager Asynchronous http client connection manager.
     * @return Asynchronous http client based on connection manager.
     */
    protected CloseableHttpAsyncClient createHttpAsyncClient(NHttpClientConnectionManager connectionManager) {
        HttpAsyncClientBuilder builder = HttpAsyncClients.custom().setConnectionManager(connectionManager);

        int socketBufferSizeInBytes = this.config.getSocketBufferSizeInBytes();
        if (socketBufferSizeInBytes > 0) {
            builder.setDefaultConnectionConfig(
                    ConnectionConfig.custom().setBufferSize(socketBufferSizeInBytes).build());
        }
        return builder.build();
    }


    /**
     * Creates HttpClient Context object based on the internal request.
     *
     * @param request The internal request.
     * @return HttpClient Context object.
     */
    protected HttpClientContext createHttpContext(Request<? extends BaseRequest> request) {
        HttpClientContext context = HttpClientContext.create();
        context.setRequestConfig(this.requestConfigBuilder.build());
        if (this.credentialsProvider != null) {
            context.setCredentialsProvider(this.credentialsProvider);
        }
        if (this.config.isProxyPreemptiveAuthenticationEnabled()) {
            AuthCache authCache = new BasicAuthCache();
            authCache.put(this.proxyHttpHost, new BasicScheme());
            context.setAuthCache(authCache);
        }
        return context;
    }

    private HttpRequestBase createHttpRequest(Request<?> request) throws IOException {
        HttpRequestBase httpRequest;
        String uri = HttpUtils.appendUri(request.getEndpoint(), request.getResourcePath()).toASCIIString();
        boolean isMultiPart = request.getMultiPartFiles() != null && request.getMultiPartFiles().size() > 0;
        if (isMultiPart) {
            if (request.getHttpMethod() == HttpMethodName.POST) {
                HttpPost postMethod = new HttpPost(uri);
                httpRequest = postMethod;
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
            } else {
                throw new YopClientException("ContentType:multipart/form-data only support Post Request");
            }
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
                    postMethod.setEntity(new StringEntity(encodedParams));
                }
            } else if (request.getHttpMethod() == HttpMethodName.DELETE) {
                httpRequest = new HttpDelete(uri);
            } else if (request.getHttpMethod() == HttpMethodName.HEAD) {
                httpRequest = new HttpHead(uri);
            } else {
                throw new YopClientException("Unknown HTTP method name: " + request.getHttpMethod());
            }
        }

        httpRequest.addHeader(Headers.HOST, HttpUtils.generateHostHeader(request.getEndpoint()));

        // Copy over any other headers already in our request
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            /*
             * HttpClient4 fills in the Content-Length header and complains if it's already present, so we skip it here.
             * We also skip the Host header to avoid sending it twice, which will interfere with some signing schemes.
             */
            if (entry.getKey().equalsIgnoreCase(Headers.CONTENT_LENGTH)
                    || entry.getKey().equalsIgnoreCase(Headers.HOST)) {
                continue;
            }

            httpRequest.addHeader(entry.getKey(), entry.getValue());
        }
        if (!isMultiPart) {
            checkNotNull(httpRequest.getFirstHeader(Headers.CONTENT_TYPE), Headers.CONTENT_TYPE + " not set");
        }
        return httpRequest;
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

    private void setUserAgent(Request<? extends BaseRequest> request) {
        request.addHeader(Headers.USER_AGENT, this.config.getUserAgent());
    }

    private void setAppKey(Request<? extends BaseRequest> request, YopCredentials yopCredentials) {
        request.addHeader(Headers.YOP_APPKEY, yopCredentials.getAppKey());
    }

    public void shutdown() {
        IdleConnectionReaper.removeConnectionManager(this.connectionManager);
        this.connectionManager.shutdown();
    }


}
