package com.yeepay.yop.sdk.model.transform;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.HttpMethodName;
import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.internal.RestartableInputStream;
import com.yeepay.yop.sdk.service.common.request.YopRequest;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * title: YopRequest序列化器<br>
 * description: <br>
 * Copyright: Copyright (c) 2020<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 16:14
 */
public abstract class AbstractYopRequestMarshaller implements RequestMarshaller<YopRequest> {

    @Override
    public Request<YopRequest> marshall(YopRequest request) {
        Request<YopRequest> internalRequest = initRequest(request);
        internalRequest.setResourcePath(request.getApiUri());
        internalRequest.setHttpMethod(HttpMethodName.valueOf(request.getHttpMethod().toUpperCase()));
        Map<String, String> customerHeaders = request.getHeaders();
        if (customerHeaders != null) {
            for (String key : customerHeaders.keySet()) {
                internalRequest.addHeader(key, customerHeaders.get(key));
            }
        }

        if (!request.getParameters().isEmpty()) {
            for (Map.Entry<String, Collection<String>> entry : request.getParameters().asMap().entrySet()) {
                internalRequest.addParameters(entry.getKey(), Lists.newArrayList(entry.getValue()));
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
                    } else {
                        throw new YopClientException("Unexpected file parameter type, name:" + name + ", type:" + value.getClass() + ".");
                    }
                }
            }
            internalRequest.setContentType(YopContentType.MULTIPART_FORM);
        } else if (request.getContent() != null) {
            //2、json上传
            if (request.getContent() instanceof String) {
                byte[] contentBytes = ((String) request.getContent()).getBytes(YopConstants.DEFAULT_CHARSET);
                internalRequest.setContent(RestartableInputStream.wrap(contentBytes));
                internalRequest.addHeader(Headers.CONTENT_LENGTH, String.valueOf(contentBytes.length));
                internalRequest.setContentType(YopContentType.JSON);
            } else if (request.getContent() instanceof InputStream) {
                //3、单文件流式上传
                internalRequest.setContent((InputStream) request.getContent());
                internalRequest.setContentType(YopContentType.OCTET_STREAM);
            }
        } else {
            //4、form表单上传
            internalRequest.setContentType(YopContentType.FORM_URL_ENCODE);
        }

        // httpclient 会自动拼multipart内容格式
        if (!YopContentType.MULTIPART_FORM.equals(internalRequest.getContentType())) {
            internalRequest.addHeader(Headers.CONTENT_TYPE, internalRequest.getContentType().getValue());
        }
        return internalRequest;
    }

    protected abstract Request<YopRequest> initRequest(YopRequest request);
}
