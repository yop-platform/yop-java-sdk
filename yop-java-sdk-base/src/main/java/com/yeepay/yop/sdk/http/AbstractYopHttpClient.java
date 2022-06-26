/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import com.google.common.collect.ImmutableSet;
import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import com.yeepay.yop.sdk.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.yeepay.yop.sdk.internal.RequestEncryptor.encrypt;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021/12/6
 */
public abstract class AbstractYopHttpClient implements YopHttpClient {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractYopHttpClient.class);

    protected static final Set<HttpMethodName> PAYLOAD_SUPPORT_METHODS =
            ImmutableSet.of(HttpMethodName.POST, HttpMethodName.PUT, HttpMethodName.DELETE);

    /**
     * Client configuration options, such as proxy settings, max retries, etc.
     */
    protected final ClientConfiguration clientConfig;

    public AbstractYopHttpClient(ClientConfiguration clientConfig) {
        checkNotNull(clientConfig, "config should not be null.");
        this.clientConfig = clientConfig;
    }

    @Override
    public <Output extends BaseResponse, Input extends BaseRequest> Output execute(Request<Input> request,
                                                                                   YopRequestConfig yopRequestConfig,
                                                                                   ExecutionContext executionContext,
                                                                                   HttpResponseHandler<Output> responseHandler) {
        Output analyzedResponse = null;
        YopHttpResponse httpResponse = null;
        try {
            preExecute(request, executionContext);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Sending Request: {}", request);
            }
            httpResponse = doExecute(request, yopRequestConfig);
            analyzedResponse = responseHandler.handle(new HttpResponseHandleContext(httpResponse, request, executionContext));
            return analyzedResponse;
        } catch (YopClientException e) {
            throw e;
        } catch (Exception e) {
            throw new YopClientException("Unable to execute HTTP request, requestId:"
                    + request.getRequestId() + ", apiUri:" + request.getResourcePath(), e);
        } finally {
            postExecute(analyzedResponse, httpResponse);
        }
    }

    /**
     * 执行请求(加密、加签)
     * @param request
     * @param yopRequestConfig
     * @param <Input>
     * @return
     */
    protected abstract <Input extends BaseRequest> YopHttpResponse doExecute(Request<Input> request, YopRequestConfig yopRequestConfig) throws IOException;

    /**
     * 请求后置处理(关闭response)
     * @param analyzedResponse
     * @param httpResponse
     * @param <Output>
     */
    protected <Output extends BaseResponse> void postExecute(Output analyzedResponse, YopHttpResponse httpResponse) {
        try {
            if (!(analyzedResponse instanceof YosDownloadResponse) && null != httpResponse) {
                httpResponse.close();
            }
        } catch (IOException e) {
            LOGGER.error("error when postExecute, ex:", e);
        }
    }

    /**
     * 加密&签名请求
     *
     * @param request
     * @param executionContext
     * @param <Input>
     */
    protected <Input extends BaseRequest> void preExecute(Request<Input> request,
                                                          ExecutionContext executionContext)
            throws ExecutionException, InterruptedException, UnsupportedEncodingException {
        // 请求标头
        addStandardHeader(request, executionContext);

        // 加密
        encryptRequest(request, executionContext);

        // 签名
        signRequest(request, executionContext);
    }

    private <Input extends BaseRequest> void addStandardHeader(Request<Input> request, ExecutionContext executionContext) {
        request.addHeader(Headers.YOP_APPKEY, executionContext.getYopCredentials().getAppKey());
        request.addHeader(Headers.USER_AGENT, this.clientConfig.getUserAgent());
    }

    /**
     * 加密请求
     *
     * @param request          请求
     * @param executionContext 执行上下文
     * @param <Input>          请求类
     */
    private <Input extends BaseRequest> void encryptRequest(Request<Input> request, ExecutionContext executionContext)
            throws ExecutionException, InterruptedException, UnsupportedEncodingException {
        if (!executionContext.isEncryptSupported()) {
            LOGGER.debug("request not encrypted for no YopEncryptor found(or maybe a rsa request)");
            return;
        }
        encrypt(request, executionContext.getEncryptor(), executionContext.getEncryptOptions().get());
    }

    /**
     * 签名强求
     *
     * @param request          请求
     * @param executionContext 执行上下文
     * @param <Input>          请求类
     */
    private <Input extends BaseRequest> void signRequest(Request<Input> request, ExecutionContext executionContext) {
        executionContext.getSigner().sign(request, executionContext.getYopCredentials(), executionContext.getSignOptions());
    }

    /**
     * 校验文件上传请求
     *
     * @param request 请求
     * @param <Input> 请求类
     * @return true if the request has MultiPartFiles
     * @throws YopClientException if the request is invalid for multipart
     */
    protected  <Input extends BaseRequest> boolean checkForMultiPart(Request<Input> request) throws YopClientException {
        boolean result = request.getMultiPartFiles() != null && request.getMultiPartFiles().size() > 0;
        if (result && !HttpMethodName.POST.equals(request.getHttpMethod())) {
            throw new YopClientException("ContentType:multipart/form-data only support Post Request");
        }
        return result;
    }

    protected  <Input extends BaseRequest> void buildHttpHeaders(Request<Input> request, HeaderBuilder headerBuilder) {
        headerBuilder.addHeader(Headers.HOST, HttpUtils.generateHostHeader(request.getEndpoint()));
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
            headerBuilder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    public interface HeaderBuilder {
        void addHeader(String key, String value);
    }

}
