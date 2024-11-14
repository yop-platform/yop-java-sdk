package com.yeepay.yop.sdk.http.analyzer;

import com.yeepay.yop.sdk.exception.YopHttpException;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.http.HttpResponseAnalyzer;
import com.yeepay.yop.sdk.http.HttpResponseHandleContext;
import com.yeepay.yop.sdk.http.HttpStatus;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopErrorResponse;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yeepay.yop.sdk.constants.CharacterConstants.SLASH;

/**
 * title:YopErrorResponseAnalyzer <br>
 * description: HTTP error response handler for YOP responses.<br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/29 17:53
 */
public class YopErrorResponseAnalyzer implements HttpResponseAnalyzer {

    private static final YopErrorResponseAnalyzer INSTANCE = new YopErrorResponseAnalyzer();

    public static YopErrorResponseAnalyzer getInstance() {
        return INSTANCE;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(YopErrorResponseAnalyzer.class);

    private YopErrorResponseAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        YopHttpResponse httpResponse = context.getResponse();
        int statusCode = httpResponse.getStatusCode();
        // 2xx
        if (statusCode / 100 == HttpStatus.SC_OK / 100 && statusCode != HttpStatus.SC_NO_CONTENT) {
            // not an error
            return false;
        }
        String resource = context.getOriginRequest().getEndpoint() + context.getOriginRequest().getResourcePath();
        // 5xx
        if (statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR && statusCode != HttpStatus.SC_BAD_GATEWAY) {
            YopServiceException yse = null;
            String content = httpResponse.readContent();
            if (null != content) {
                YopErrorResponse yopErrorResponse = null;
                try {
                    yopErrorResponse = JsonUtils.loadFrom(content, YopErrorResponse.class);
                } catch (Exception ex) {
                    LOGGER.warn("Response Illegal, YopErrorResponse ParseFail, content:" + content, ex);
                }
                if (yopErrorResponse != null && yopErrorResponse.getMessage() != null) {
                    yse = new YopServiceException(yopErrorResponse.getMessage());
                    yse.setErrorCode(yopErrorResponse.getCode());
                    yse.setSubErrorCode(yopErrorResponse.getSubCode());
                    yse.setSubMessage(yopErrorResponse.getSubMessage());
                    yse.setRequestId(yopErrorResponse.getRequestId());
                    yse.setDocUrl(yopErrorResponse.getDocUrl());
                }
            }
            if (yse == null) {
                yse = new YopServiceException(httpResponse.getStatusText());
                yse.setRequestId(response.getMetadata().getYopRequestId());
            }
            yse.setStatusCode(httpResponse.getStatusCode());
            yse.setErrorType(YopServiceException.ErrorType.Service);
            throw yse;
        } else if (statusCode == HttpStatus.SC_BAD_GATEWAY || statusCode == HttpStatus.SC_NOT_FOUND) {
            throw new YopHttpException("ResponseError, Unexpected Response, statusCode:" + statusCode + ", resource:" + resource);
        } else {// 4xx
            final YopServiceException invokeEx = new YopServiceException("ReqParam Illegal, Bad Request, statusCode:" + statusCode + ", resource:" + resource);
            invokeEx.setErrorType(YopServiceException.ErrorType.Client);
            throw invokeEx;
        }
    }
}
