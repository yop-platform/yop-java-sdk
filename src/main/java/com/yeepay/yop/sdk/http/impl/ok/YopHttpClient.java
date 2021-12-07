/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http.impl.ok;

import com.github.simonpercic.oklog3.OkLogInterceptor;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.*;
import com.yeepay.yop.sdk.internal.MultiPartFile;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.utils.HttpUtils;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private static final Logger logger = LoggerFactory.getLogger(YopHttpClient.class);
    private static final RequestBody EMPTY_BODY = RequestBody.create(new byte[0]);
    private static final OkLogInterceptor logInterceptor = OkLogInterceptor.builder()
            .withRequestHeaders(true).withResponseHeaders(true).build();

    private OkHttpClient defaultHttpClient;

    public YopHttpClient(ClientConfiguration clientConfig) {
        super(clientConfig);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
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
        if (HttpUtils.isHttpVerbose()) {
            httpClientBuilder.addInterceptor(logInterceptor);
        }
        defaultHttpClient = httpClientBuilder.build();
    }

    @Override
    public <Output extends BaseResponse, Input extends BaseRequest> Output execute(
            Request<Input> yopRequest, YopRequestConfig yopRequestConfig,
            ExecutionContext executionContext, HttpResponseHandler<Output> responseHandler) {
        try {
            configYopRequest(yopRequest, yopRequestConfig, executionContext);
            OkHttpClient httpClient = createHttpClient(yopRequestConfig);
            logger.debug("Sending Request: {}", yopRequest);
            final Response httpResponse = httpClient.newCall(createHttpRequest(yopRequest)).execute();
            return responseHandler.handle(new HttpResponseHandleContext(
                    new YopOkHttpResponse(httpResponse), yopRequest, yopRequestConfig, executionContext));
        } catch (Exception e) {
            YopClientException yop;
            if (e instanceof YopClientException) {
                yop = (YopClientException) e;
            } else {
                yop = new YopClientException("Unable to execute HTTP request", e);
            }
            throw yop;
        }
    }

    private OkHttpClient createHttpClient(YopRequestConfig yopRequestConfig) {
        // 定制请求级参数
        if ((yopRequestConfig.getConnectTimeout() > 0 &&
                yopRequestConfig.getConnectTimeout() != defaultHttpClient.connectTimeoutMillis()) ||
                (yopRequestConfig.getReadTimeout() > 0
                        && yopRequestConfig.getReadTimeout() != defaultHttpClient.readTimeoutMillis())) {
            OkHttpClient.Builder requestClientBuilder = defaultHttpClient.newBuilder();
            if (yopRequestConfig.getConnectTimeout() > 0) {
                requestClientBuilder.connectTimeout(yopRequestConfig.getConnectTimeout(), TimeUnit.MILLISECONDS);
            }
            if (yopRequestConfig.getReadTimeout() > 0) {
                requestClientBuilder.readTimeout(yopRequestConfig.getReadTimeout(), TimeUnit.MILLISECONDS);
            }
            return requestClientBuilder.build();
        }
        return defaultHttpClient;
    }

    private <Input extends BaseRequest> okhttp3.Request createHttpRequest(Request<Input> request) throws IOException {
        final okhttp3.Request.Builder httpRequestBuilder = new okhttp3.Request.Builder();
        String uri = HttpUtils.appendUri(request.getEndpoint(), request.getResourcePath()).toASCIIString();
        boolean isMultiPart = request.getMultiPartFiles() != null && request.getMultiPartFiles().size() > 0;

        // 文件上传
        if (isMultiPart) {
            if (request.getHttpMethod() == HttpMethodName.POST) {
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
                        bodyBuilder.addFormDataPart(name, multiPartFile.getFileName(),
                                RequestBody.create(IOUtils.toByteArray(multiPartFile.getInputStream())));
                    }
                }
                httpRequestBuilder.post(bodyBuilder.build());
            } else {
                throw new YopClientException("ContentType:multipart/form-data only support Post Request");
            }
        } else {
            // queryParams
            String encodedParams = HttpUtils.encodeParameters(request, false);
            boolean hasBodyParams = null != request.getContent();
            boolean useQueryParams = StringUtils.isNotBlank(encodedParams) &&
                    (!PAYLOAD_SUPPORT_METHODS.contains(request.getHttpMethod()) || hasBodyParams);
            if (useQueryParams) {
                uri += "?" + encodedParams;
            }

            // bodyParams
            RequestBody requestBody = EMPTY_BODY;
            if (hasBodyParams) {
                requestBody = RequestBody.create(IOUtils.toByteArray(request.getContent()));
            } else if (StringUtils.isNotBlank(encodedParams) && !useQueryParams) {
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
                    throw new YopClientException("Unknown HTTP method name: " + request.getHttpMethod());
            }
        }

        // headers
        httpRequestBuilder.addHeader(Headers.HOST, HttpUtils.generateHostHeader(request.getEndpoint()));
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
            httpRequestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        final okhttp3.Request httpRequest = httpRequestBuilder.url(uri).build();
        if (!isMultiPart) {
            checkNotNull(httpRequest.header(Headers.CONTENT_TYPE), Headers.CONTENT_TYPE + " not set");
        }
        return httpRequest;
    }

    @Override
    public void shutdown() {
        //nothing to do
    }
}
