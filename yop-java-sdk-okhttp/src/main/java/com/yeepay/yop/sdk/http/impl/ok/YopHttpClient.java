/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http.impl.ok;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.AbstractYopHttpClient;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.internal.MultiPartFile;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.utils.HttpUtils;
import okhttp3.Credentials;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021/12/1
 */
public class YopHttpClient extends AbstractYopHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopHttpClient.class);

    private static final RequestBody EMPTY_BODY = RequestBody.create(new byte[0]);

    private static class HttpLogger implements HttpLoggingInterceptor.Logger {
        @Override
        public void log(@NotNull String s) {
            LOGGER.info(s);
        }
    }

    private static final HttpLoggingInterceptor logInterceptor =
            new HttpLoggingInterceptor(new HttpLogger()).setLevel(HttpLoggingInterceptor.Level.BODY);

    /**
     * OkHttpClients Should Be Shared
     *
     * OkHttp performs best when you create a single `OkHttpClient` instance and reuse it for all of
     * your HTTP calls. This is because each client holds its own connection pool and thread pools.
     * Reusing connections and threads reduces latency and saves memory. Conversely, creating a client
     * for each request wastes resources on idle pools.
     */
    private static final OkHttpClient sharedHttpClient;

    static {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        if (HttpUtils.isHttpVerbose()) {
            httpClientBuilder.addNetworkInterceptor(logInterceptor);
        }
        sharedHttpClient = httpClientBuilder.build();
    }

    private final OkHttpClient customHttpClient;

    public YopHttpClient(ClientConfiguration clientConfig) {
        super(clientConfig);
        OkHttpClient.Builder httpClientBuilder = sharedHttpClient.newBuilder()
                .connectTimeout(clientConfig.getConnectionTimeoutInMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(clientConfig.getSocketTimeoutInMillis(), TimeUnit.MILLISECONDS);

        // http 代理
        String proxyHost = clientConfig.getProxyHost();
        int proxyPort = clientConfig.getProxyPort();
        if (proxyHost != null && proxyPort > 0) {
            httpClientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
            String proxyUsername = clientConfig.getProxyUsername();
            String proxyPassword = clientConfig.getProxyPassword();
            if (proxyUsername != null && proxyPassword != null) {
                httpClientBuilder.proxyAuthenticator((route, response) -> {
                    // 设置代理服务器用户名、密码
                    final String credential = Credentials.basic(proxyUsername, proxyPassword, StandardCharsets.UTF_8);
                    return response.request().newBuilder().header("Proxy-Authorization", credential).build();
                });
            }
        }
        this.customHttpClient = httpClientBuilder.build();
    }

    @Override
    protected <Input extends BaseRequest> YopHttpResponse doExecute(Request<Input> request, YopRequestConfig yopRequestConfig) throws IOException {
        return new YopOkHttpResponse(buildHttpClient(yopRequestConfig).newCall(buildHttpRequest(request)).execute());
    }

    private OkHttpClient buildHttpClient(YopRequestConfig yopRequestConfig) {
        // 定制请求级参数
        if ((yopRequestConfig.getConnectTimeout() > 0 &&
                yopRequestConfig.getConnectTimeout() != customHttpClient.connectTimeoutMillis()) ||
                (yopRequestConfig.getReadTimeout() > 0
                        && yopRequestConfig.getReadTimeout() != customHttpClient.readTimeoutMillis())) {
            OkHttpClient.Builder requestClientBuilder = customHttpClient.newBuilder();
            if (yopRequestConfig.getConnectTimeout() > 0) {
                requestClientBuilder.connectTimeout(yopRequestConfig.getConnectTimeout(), TimeUnit.MILLISECONDS);
            }
            if (yopRequestConfig.getReadTimeout() > 0) {
                requestClientBuilder.readTimeout(yopRequestConfig.getReadTimeout(), TimeUnit.MILLISECONDS);
            }
            return requestClientBuilder.build();
        }
        return customHttpClient;
    }

    private <Input extends BaseRequest> okhttp3.Request buildHttpRequest(Request<Input> request) throws IOException {
        boolean isMultiPart = checkForMultiPart(request);

        String queryParams = "";
        final okhttp3.Request.Builder httpRequestBuilder = new okhttp3.Request.Builder();

        // 文件上传
        if (isMultiPart) {
            httpRequestBuilder.post(buildMultiPartRequest(request));
        } else {
            // queryParams
            String encodedParams = HttpUtils.encodeParameters(request, false);
            boolean hasBodyParams = null != request.getContent();
            boolean hasEncodedParams = StringUtils.isNotBlank(encodedParams);
            boolean useQueryParams = hasEncodedParams &&
                    (!PAYLOAD_SUPPORT_METHODS.contains(request.getHttpMethod()) || hasBodyParams);
            if (useQueryParams) {
                queryParams = "?" + encodedParams;
            }

            // bodyParams
            RequestBody requestBody = EMPTY_BODY;
            if (hasBodyParams) {
                try (InputStream content = request.getContent()){
                    requestBody = RequestBody.create(IOUtils.toByteArray(content));
                }
            } else if (hasEncodedParams && !useQueryParams) {
                requestBody = RequestBody.create(encodedParams.getBytes(YopConstants.DEFAULT_CHARSET));
            }

            // methods
            switch (request.getHttpMethod()) {
                case PUT:
                    httpRequestBuilder.put(requestBody);
                    break;
                case POST:
                    httpRequestBuilder.post(requestBody);
                    break;
                case GET:
                    httpRequestBuilder.get();
                    break;
                case DELETE:
                    if (!EMPTY_BODY.equals(requestBody)) {
                        httpRequestBuilder.delete(requestBody);
                    } else {
                        httpRequestBuilder.delete();
                    }
                    break;
                case HEAD:
                    httpRequestBuilder.head();
                    break;
                default:
                    throw new YopClientException("ReqParam Illegal, HttpMethod NotSupport, name:" +
                            request.getHttpMethod() + ", request:" + request);
            }
        }

        // headers
        buildHttpHeaders(request, new OkHeaderBuilder(httpRequestBuilder));

        // the request
        final okhttp3.Request httpRequest = httpRequestBuilder.url(HttpUtils.appendUri(request.getEndpoint()
                , request.getResourcePath()).toASCIIString() + queryParams)
                .build();

        if (!isMultiPart) {
            checkNotNull(httpRequest.header(Headers.CONTENT_TYPE), Headers.CONTENT_TYPE + " not set");
        }
        return httpRequest;
    }

    private <Input extends BaseRequest> MultipartBody buildMultiPartRequest(Request<Input> request) throws IOException {
        final MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (Map.Entry<String, List<String>> entry : request.getParameters().entrySet()) {
            String name = entry.getKey();
            for (String value : entry.getValue()) {
                bodyBuilder.addFormDataPart(HttpUtils.normalize(name), HttpUtils.normalize(value));
            }
        }
        for (Map.Entry<String, List<MultiPartFile>> entry : request.getMultiPartFiles().entrySet()) {
            String name = entry.getKey();
            for (MultiPartFile multiPartFile : entry.getValue()) {
                try (InputStream inputStream = multiPartFile.getInputStream()){
                    bodyBuilder.addFormDataPart(name, multiPartFile.getFileName(),
                            RequestBody.create(IOUtils.toByteArray(inputStream)));
                }
            }
        }
        return bodyBuilder.build();
    }

    @Override
    public void shutdown() {
        //nothing to do
    }

    static class OkHeaderBuilder implements HeaderBuilder {
        private okhttp3.Request.Builder builder;

        public OkHeaderBuilder(okhttp3.Request.Builder builder) {
            this.builder = builder;
        }

        @Override
        public void addHeader(String key, String value) {
            builder.addHeader(key, value);
        }
    }
}
