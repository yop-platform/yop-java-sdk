package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.client.*;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.HttpResponseAnalyzerSupport;
import com.yeepay.yop.sdk.http.HttpResponseHandler;
import com.yeepay.yop.sdk.http.handler.DefaultHttpResponseHandler;
import com.yeepay.yop.sdk.model.transform.RequestMarshaller;
import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.request.YopRequestMarshaller;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.service.common.response.YosUploadResponse;

/**
 * title: YopClientImpl<br>
 * description: <br>
 * Copyright: Copyright (c) 2020<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 16:13
 */
public class YopClientImpl implements YopClient {

    private final ClientHandler clientHandler;

    YopClientImpl(ClientParams clientParams) {
        this.clientHandler = new ClientHandlerImpl(new ClientHandlerParams().withClientParams(clientParams));
    }

    @Override
    public YopResponse request(YopRequest request) {
        if (request == null) {
            throw new YopClientException("request is required.");
        }
        RequestMarshaller<YopRequest> requestMarshaller = YopRequestMarshaller.getInstance();
        HttpResponseHandler<YopResponse> responseHandler = new DefaultHttpResponseHandler<YopResponse>(YopResponse.class,
                HttpResponseAnalyzerSupport.getAnalyzerChain());
        return clientHandler.execute(new ClientExecutionParams<YopRequest, YopResponse>()
                .withInput(request)
                .withRequestMarshaller(requestMarshaller)
                .withResponseHandler(responseHandler));
    }

    @Override
    public YosDownloadResponse download(YopRequest request) {
        if (request == null) {
            throw new YopClientException("request is required.");
        }
        RequestMarshaller<YopRequest> requestMarshaller = YopRequestMarshaller.getInstance();
        HttpResponseHandler<YosDownloadResponse> responseHandler = new DefaultHttpResponseHandler<YosDownloadResponse>(YosDownloadResponse.class,
                HttpResponseAnalyzerSupport.getYosDownloadAnalyzerChain());
        return clientHandler.execute(new ClientExecutionParams<YopRequest, YosDownloadResponse>()
                .withInput(request)
                .withRequestMarshaller(requestMarshaller)
                .withResponseHandler(responseHandler));
    }

    @Override
    public YosUploadResponse upload(YopRequest request) {
        if (request == null) {
            throw new YopClientException("request is required.");
        }
        if (request.getMultipartFiles().isEmpty() && request.getContent() == null) {
            throw new YopClientException("request.multiPartFiles and request.content both are empty.");
        }
        RequestMarshaller<YopRequest> requestMarshaller = YopRequestMarshaller.getInstance();
        HttpResponseHandler<YosUploadResponse> responseHandler = new DefaultHttpResponseHandler<YosUploadResponse>(YosUploadResponse.class,
                HttpResponseAnalyzerSupport.getYosUploadAnalyzerChain());
        return clientHandler.execute(new ClientExecutionParams<YopRequest, YosUploadResponse>()
                .withInput(request)
                .withRequestMarshaller(requestMarshaller)
                .withResponseHandler(responseHandler));
    }

}
