package com.yeepay.yop.sdk.service.common.request;

import com.yeepay.g3.core.yop.sdk.sample.YopConstants;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;
import com.yeepay.g3.core.yop.sdk.sample.http.Headers;
import com.yeepay.g3.core.yop.sdk.sample.http.HttpMethodName;
import com.yeepay.g3.core.yop.sdk.sample.internal.DefaultRequest;
import com.yeepay.g3.core.yop.sdk.sample.internal.Request;
import com.yeepay.g3.core.yop.sdk.sample.internal.RestartableInputStream;
import com.yeepay.g3.core.yop.sdk.sample.model.transform.RequestMarshaller;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * title: YopRequest序列化器<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2020<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 16:14
 */
public class YopRequestMarshaller implements RequestMarshaller<YopRequest> {

    private static YopRequestMarshaller INSTANCE = new YopRequestMarshaller();

    public static YopRequestMarshaller getInstance() {
        return INSTANCE;
    }

    @Override
    public Request<YopRequest> marshall(YopRequest request) {
        String[] pathParts = StringUtils.split(request.getApiUri(), "/");
        Request<YopRequest> internalRequest = new DefaultRequest<YopRequest>(pathParts[2]);
        if (StringUtils.equals(pathParts[0], "yos")) {
            internalRequest.assignYos();
        }
        internalRequest.setResourcePath(request.getApiUri());
        internalRequest.setHttpMethod(HttpMethodName.valueOf(request.getHttpMethod().toUpperCase()));
        internalRequest.addHeader(Headers.YOP_REQUEST_ID, UUID.randomUUID().toString());

        if (!request.getParameters().isEmpty()) {
            for (Map.Entry<String, Collection<String>> entry : request.getParameters().asMap().entrySet()) {
                internalRequest.addParameters(entry.getKey(), new ArrayList<String>(entry.getValue()));
            }
        }
        //1、多文件上传
        if (!request.getMultipartFiles().isEmpty()) {
            for (Map.Entry<String, Collection<Object>> entry : request.getMultipartFiles().asMap().entrySet()) {
                String name = entry.getKey();
                for (Object value : entry.getValue()) {
                    if (value instanceof File) {
                        internalRequest.addMultiPartFile(name, (File) value);
                    } else if (value instanceof InputStream) {
                        internalRequest.addMultiPartFile(name, (InputStream) value);
                    }
                    throw new YopClientException("Unexpected file parameter type, name:" + name + ", type:" + value.getClass() + ".");
                }
            }
        } else if (request.getContent() != null) {
            //2、json上传
            if (request.getContent() instanceof String) {
                byte[] contentBytes = ((String) request.getContent()).getBytes(YopConstants.DEFAULT_CHARSET);
                internalRequest.setContent(RestartableInputStream.wrap(contentBytes));
                internalRequest.addHeader(Headers.CONTENT_TYPE, "application/json");
                internalRequest.addHeader(Headers.CONTENT_LENGTH, String.valueOf(contentBytes.length));
            } else if (request.getContent() instanceof InputStream) {
                //3、单文件流式上传
                internalRequest.setContent((InputStream) request.getContent());
            }
        } else {
            //4、form表单上传
            internalRequest.addHeader(Headers.CONTENT_TYPE, "application/x-www-form-urlencoded");
        }
        return internalRequest;
    }
}
