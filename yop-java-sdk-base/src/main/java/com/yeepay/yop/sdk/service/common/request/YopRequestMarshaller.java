package com.yeepay.yop.sdk.service.common.request;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.HttpMethodName;
import com.yeepay.yop.sdk.internal.DefaultRequest;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.internal.RestartableInputStream;
import com.yeepay.yop.sdk.model.transform.RequestMarshaller;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static com.yeepay.yop.sdk.YopConstants.YOP_HTTP_CONTENT_TYPE_FORM;
import static com.yeepay.yop.sdk.YopConstants.YOP_HTTP_CONTENT_TYPE_JSON;

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
public class YopRequestMarshaller implements RequestMarshaller<YopRequest> {

    private static final YopRequestMarshaller INSTANCE = new YopRequestMarshaller();

    public static YopRequestMarshaller getInstance() {
        return INSTANCE;
    }

    @Override
    public Request<YopRequest> marshall(YopRequest request) {
        String[] pathParts = StringUtils.split(request.getApiUri(), "/");
        Request<YopRequest> internalRequest = new DefaultRequest<YopRequest>(request, pathParts[2]);
        if (StringUtils.equals(pathParts[0], "yos")) {
            internalRequest.assignYos();
        }
        internalRequest.setResourcePath(request.getApiUri());
        internalRequest.setHttpMethod(HttpMethodName.valueOf(request.getHttpMethod().toUpperCase()));
        Map<String, String> customerHeaders = request.getRequestConfig().getCustomRequestHeaders();
        if (customerHeaders != null) {
            for (String key : customerHeaders.keySet()) {
                internalRequest.addHeader(key, customerHeaders.get(key));
            }
        }
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
                        resetStreamIfNecessary((InputStream) value);
                        internalRequest.addMultiPartFile(name, (InputStream) value);
                    } else {
                        throw new YopClientException("Unexpected file parameter type, name:" + name + ", type:" + value.getClass() + ".");
                    }
                }
            }
        } else if (request.getContent() != null) {
            //2、json上传
            if (request.getContent() instanceof String) {
                byte[] contentBytes = ((String) request.getContent()).getBytes(YopConstants.DEFAULT_CHARSET);
                internalRequest.setContent(RestartableInputStream.wrap(contentBytes));
                internalRequest.addHeader(Headers.CONTENT_TYPE, YOP_HTTP_CONTENT_TYPE_JSON);
                internalRequest.addHeader(Headers.CONTENT_LENGTH, String.valueOf(contentBytes.length));
            } else if (request.getContent() instanceof InputStream) {
                //3、单文件流式上传
                final InputStream content = (InputStream) request.getContent();
                resetStreamIfNecessary((InputStream) request.getContent());
                internalRequest.setContent(content);
            }
        } else {
            //4、form表单上传
            internalRequest.addHeader(Headers.CONTENT_TYPE, YOP_HTTP_CONTENT_TYPE_FORM);
        }
        return internalRequest;
    }

    private void resetStreamIfNecessary(InputStream content) {
        if (content instanceof RestartableInputStream) {
            ((RestartableInputStream) content).restart();
        }
    }
}
