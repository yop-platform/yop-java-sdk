package com.yeepay.yop.sdk.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.HttpMethodName;
import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.utils.JsonUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of the {@linkplain com.yeepay.yop.sdk.internal.Request} interface.
 * <p>
 * This class is only intended for internal use inside the AWS client libraries.
 * Callers shouldn't ever interact directly with objects of this class.
 */
public class DefaultRequest<T extends BaseRequest> implements Request<T> {

    /**
     * the requestId per request
     */
    private String requestId;

    /**
     * the http content-type
     */
    private YopContentType contentType;

    /**
     * The resource path being requested
     */
    private String resourcePath;

    /**
     * Map of the parameters being sent as part of this request.
     * <p>
     * Note that a LinkedHashMap is used, since we want to preserve the
     * insertion order so that members of a list parameter will still be ordered
     * by their indices when they are marshalled into the query string.
     * <p>
     * Lists values in this Map must use an implementation that allows
     * null values to be present.
     */
    private final Map<String, List<String>> parameters = Maps.newLinkedHashMap();

    /**
     * Map of the files being sent as part of this request
     */
    private final Map<String, List<MultiPartFile>> multiPartFiles = Maps.newLinkedHashMap();

    /**
     * Map of the headers included in this request
     */
    private final Map<String, String> headers = Maps.newHashMap();

    /**
     * The service endpoint to which this request should be sent
     */
    private URI endpoint;

    /**
     * The name of the service to which this request is being sent
     */
    private final String serviceName;

    /**
     * The original, user facing request object which this internal request
     * object is representing
     */
    private final T originalRequest;

    /**
     * The HTTP method to use when sending this request.
     */
    private HttpMethodName httpMethod = HttpMethodName.POST;

    /**
     * An optional stream from which to read the request payload.
     */
    private InputStream content;

    /**
     * An optional time offset to account for clock skew
     */
    private int timeOffset;

    /**
     * whether this request is assigned yos
     */
    private boolean yosAssigned = false;

    /**
     * Constructs a new DefaultRequest with the specified service name and the
     * original, user facing request object.
     *
     * @param serviceName     The name of the service to which this request is being sent.
     * @param originalRequest The original, user facing, AWS request being represented by
     *                        this internal request object.
     */
    public DefaultRequest(T originalRequest, String serviceName) {
        this.serviceName = serviceName;
        this.originalRequest = originalRequest;
        this.requestId = UUID.randomUUID().toString();
        this.headers.put(Headers.YOP_REQUEST_ID, this.requestId);
    }

    /**
     * Constructs a new DefaultRequest with the specified service name and no
     * specified original, user facing request object.
     *
     * @param serviceName The name of the service to which this request is being sent.
     */
    public DefaultRequest(String serviceName) {
        this(null, serviceName);
    }

    /**
     * Constructs a new DefaultRequest with the original, user facing request object.
     *
     * @param @param originalRequest The original, user facing, AWS request being represented by
     *                               this internal request object.
     */
    public DefaultRequest(T originalRequest) {
        this(originalRequest, CharacterConstants.EMPTY);
    }


    /**
     * Returns the original, user facing request object which this internal
     * request object is representing.
     *
     * @return The original, user facing request object which this request
     * object is representing.
     */
    public T getOriginalRequest() {
        return originalRequest;
    }

    /**
     * @see Request#addHeader(String, String)
     */
    @Override
    public void addHeader(String name, String value) {
        // 支持指定请求id
        if (Headers.YOP_REQUEST_ID.equals(name)) {
            this.requestId = value;
        }
        headers.put(name, value);
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public YopContentType getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(YopContentType contentType) {
        this.contentType = contentType;
    }

    /**
     * @see Request#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @see Request#setResourcePath(String)
     */
    @Override
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * @see Request#getResourcePath()
     */
    @Override
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * @see Request#addParameter(String, String)
     */
    @Override
    public void addParameter(String name, String value) {
        List<String> paramList = parameters.get(name);
        if (paramList == null) {
            paramList = Lists.newArrayList();
            parameters.put(name, paramList);
        }
        paramList.add(value);
    }

    /**
     * @see Request#addParameters(String, List)
     */
    @Override
    public void addParameters(String name, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            addParameter(name, value);
        }
    }

    /**
     * @see Request#getParameters()
     */
    @Override
    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    /**
     * @see Request#withParameter(java.lang.String, java.lang.String)
     */
    @Override
    public Request<T> withParameter(String name, String value) {
        addParameter(name, value);
        return this;
    }

    @Override
    public Map<String, List<MultiPartFile>> getMultiPartFiles() {
        return multiPartFiles;
    }

    @Override
    public void setMultiPartFiles(Map<String, List<MultiPartFile>> multiPartFiles) {
        this.multiPartFiles.clear();
        this.multiPartFiles.putAll(multiPartFiles);
    }

    @Override
    public Request<T> withMultiPartFile(String name, File file) {
        addMultiPartFile(name, file);
        return this;
    }

    @Override
    public void addMultiPartFile(String name, File file) {
        List<MultiPartFile> files = multiPartFiles.get(name);
        if (files == null) {
            files = Lists.newArrayList();
            multiPartFiles.put(name, files);
        }
        try {
            files.add(new MultiPartFile(file));
        } catch (Exception ex) {
            throw new YopClientException("add file failed.", ex);
        }
    }

    @Override
    public void addMultiPartFile(String name, InputStream in) {
        List<MultiPartFile> files = multiPartFiles.get(name);
        if (files == null) {
            files = Lists.newArrayList();
            multiPartFiles.put(name, files);
        }
        try {
            files.add(new MultiPartFile(in));
        } catch (Exception ex) {
            throw new YopClientException("add file failed", ex);
        }
    }

    /**
     * @see Request#getHttpMethod()
     */
    @Override
    public HttpMethodName getHttpMethod() {
        return httpMethod;
    }

    /**
     * @see Request#setHttpMethod(HttpMethodName)
     */
    @Override
    public void setHttpMethod(HttpMethodName httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * @see Request#setEndpoint(java.net.URI)
     */
    @Override
    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @see Request#getEndpoint()
     */
    @Override
    public URI getEndpoint() {
        return endpoint;
    }

    /**
     * @see Request#getServiceName()
     */
    @Override
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @see Request#getContent()
     */
    @Override
    public InputStream getContent() {
        return content;
    }

    /**
     * @see Request#setContent(InputStream)
     */
    @Override
    public void setContent(InputStream content) {
        this.content = content;
    }

    /**
     * @see Request#setHeaders(java.util.Map)
     */
    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
    }

    /**
     * @see Request#setParameters(java.util.Map)
     */
    @Override
    public void setParameters(Map<String, List<String>> parameters) {
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }

    /**
     * @see Request#getTimeOffset
     */
    @Override
    public int getTimeOffset() {
        return timeOffset;
    }

    /**
     * @see Request#setTimeOffset(int)
     */
    @Override
    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

    /**
     * @see Request#setTimeOffset(int)
     */
    @Override
    public Request<T> withTimeOffset(int timeOffset) {
        setTimeOffset(timeOffset);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getHttpMethod()).append(" ");
        builder.append(getEndpoint()).append(" ");
        String resourcePath = getResourcePath();

        if (resourcePath == null) {
            builder.append("/");
        } else {
            if (!resourcePath.startsWith("/")) {
                builder.append("/");
            }
            builder.append(resourcePath);
        }
        builder.append(" ");
        if (!getParameters().isEmpty()) {
            builder.append("Parameters: (")
                    .append(JsonUtils.toJsonString(parameters));
        }

        if (!getHeaders().isEmpty()) {
            builder.append("Headers: (");
            for (String key : getHeaders().keySet()) {
                String value = getHeaders().get(key);
                builder.append(key).append(": ").append(value).append(", ");
            }
            builder.append(") ");
        }

        return builder.toString();
    }

    @Override
    public T getOriginalRequestObject() {
        return originalRequest;
    }

    @Override
    public boolean isYosRequest() {
        return yosAssigned || (multiPartFiles != null && multiPartFiles.size() > 0);
    }

    @Override
    public void assignYos() {
        this.yosAssigned = true;
    }


}
