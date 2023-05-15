package com.yeepay.yop.sdk.service.common.request;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;
import com.yeepay.g3.core.yop.sdk.sample.model.BaseRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * title: Yop请求<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2020<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 16:14
 */
public class YopRequest extends BaseRequest {

    private static final long serialVersionUID = -1L;

    private String apiUri;

    private String httpMethod;

    private Multimap<String, String> parameters = ArrayListMultimap.create();

    private Multimap<String, Object> multipartFiles = ArrayListMultimap.create();

    private Object content;

    public YopRequest(String apiUri, String httpMethod) {
        this.apiUri = apiUri;
        this.httpMethod = httpMethod;
    }

    public String getApiUri() {
        return apiUri;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Multimap<String, String> getParameters() {
        return parameters;
    }

    public Multimap<String, Object> getMultipartFiles() {
        return multipartFiles;
    }

    public Object getContent() {
        return content;
    }

    public YopRequest addParameter(String name, String value) {
        if (StringUtils.isEmpty(name)) {
            throw new YopClientException("parameter name:" + name + " should not be empty.");
        }
        if (value == null) {
            throw new YopClientException("parameter value for name:" + name + " can't be null.");
        }
        parameters.put(name, value);
        return this;
    }

    public YopRequest addParameters(String name, List<String> values) {
        if (StringUtils.isEmpty(name)) {
            throw new YopClientException("parameter name:" + name + " should not be empty.");
        }
        if (values == null) {
            throw new YopClientException("parameter value for name:" + name + " can't be null.");
        }
        parameters.putAll(name, values);
        return this;
    }

    public YopRequest addParameter(String name, Object value) {
        if (StringUtils.isEmpty(name)) {
            throw new YopClientException("parameter name:" + name + " should not be empty.");
        }
        if (value == null) {
            throw new YopClientException("parameter value for name:" + name + " can't be null.");
        }
        if (value instanceof Collection) {
            for (Object o : (Collection) value) {
                if (o != null) {
                    parameters.put(name, o.toString());
                }
            }
        } else if (value.getClass().isArray()) {
            int len = Array.getLength(value);
            for (int i = 0; i < len; i++) {
                Object o = Array.get(value, i);
                if (o != null) {
                    parameters.put(name, o.toString());
                }
            }
        } else {
            parameters.put(name, value.toString());
        }
        return this;
    }

    public YopRequest addMutiPartFile(String name, File file) {
        if (StringUtils.isEmpty(name)) {
            throw new YopClientException("parameter name:" + name + " should not be empty.");
        }
        if (file == null || !file.exists()) {
            throw new YopClientException("file is null or file does not exist.");
        }
        multipartFiles.put(name, file);
        return this;
    }

    public YopRequest addMultiPartFile(String name, InputStream inputStream) {
        if (StringUtils.isEmpty(name)) {
            throw new YopClientException("parameter name:" + name + " should not be empty.");
        }
        if (inputStream == null) {
            throw new YopClientException("inputStream for name:" + name + " should not be null.");
        }
        multipartFiles.put(name, inputStream);
        return this;
    }

    public YopRequest setContent(String content) {
        if (StringUtils.isEmpty(content)) {
            throw new YopClientException("content should not be empty");
        }
        this.content = content;
        return this;
    }

    public YopRequest setStream(InputStream inputStream) {
        if (inputStream == null) {
            throw new YopClientException("inputStream for content should not be null");
        }
        this.content = inputStream;
        return this;
    }

    public YopRequest withApiUri(String apiUri){
        this.apiUri = apiUri;
        return this;
    }

    public YopRequest withHttpMethod(String httpMethod){
        this.httpMethod = httpMethod;
        return this;
    }

    @Override
    public String getOperationId() {
        return apiUri;
    }
}
