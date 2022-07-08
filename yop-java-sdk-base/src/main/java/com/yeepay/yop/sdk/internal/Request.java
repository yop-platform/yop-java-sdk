package com.yeepay.yop.sdk.internal;

import com.yeepay.yop.sdk.http.HttpMethodName;
import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.model.BaseRequest;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * title: 请求<br>
 * description:
 * <p>
 * Represents a request being sent to Yop Api-GateWay, including the
 * parameters being sent as part of the request, the endpoint to which the
 * request should be sent, etc
 * </p>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/13 17:03
 */
public interface Request<T extends BaseRequest> {

    /**
     * current request-id
     *
     * @return
     */
    String getRequestId();

    /**
     * http content-type
     *
     * @return the YopContentType
     */
    YopContentType getContentType();

    /**
     * set the http content-type
     *
     * @param contentType the YopContentType
     */
    void setContentType(YopContentType contentType);

    /**
     * Returns a map of all the headers included in this request.
     *
     * @return A map of all the headers included in this request.
     */
    Map<String, String> getHeaders();

    /**
     * Sets all headers, clearing any existing ones.
     *
     * @param headers headers
     */
    void setHeaders(Map<String, String> headers);

    /**
     * Sets the specified header for this request.
     *
     * @param name  The name of the header to set.
     * @param value The header's value.
     */
    void addHeader(String name, String value);

    /**
     * Returns the path to the resource being requested.
     *
     * @return The path to the resource being requested.
     */
    String getResourcePath();

    /**
     * Sets the path to the resource being requested.
     *
     * @param path The path to the resource being requested.
     */
    void setResourcePath(String path);

    /**
     * Returns a map of all parameters in this request.
     *
     * @return A map of all parameters in this request.
     */
    Map<String, List<String>> getParameters();

    /**
     * Adds the specified request parameter to this request, and returns the
     * updated request object.
     *
     * @param name  The name of the request parameter.
     * @param value The value of the request parameter.
     * @return The updated request object.
     */
    Request<T> withParameter(String name, String value);

    /**
     * Sets all parameters, clearing any existing values.
     * <p>
     * Note that List values within the parameters Map must use an implementation that supports null
     * values.
     *
     * @param parameters the request parameters.
     */
    void setParameters(Map<String, List<String>> parameters);

    /**
     * Adds the specified request parameter and list of values to this request.
     *
     * @param name   The name of the request parameter.
     * @param values The value of the request parameter.
     */
    void addParameters(String name, List<String> values);

    /**
     * Adds the specified request parameter to this request.
     *
     * @param name  The name of the request parameter.
     * @param value The value of the request parameter.
     */
    void addParameter(String name, String value);

    /**
     * Return a map of all multipart files in this request
     *
     * @return A map of all multipart files in this request
     */
    Map<String, List<MultiPartFile>> getMultiPartFiles();

    /**
     * Sets multipart files, clearing any existing values.
     * <p>
     * Note that List values within the parameters Map must use an implementation that supports null
     * values.
     *
     * @param multiPartFiles the request multipart files.
     */
    void setMultiPartFiles(Map<String, List<MultiPartFile>> multiPartFiles);

    /**
     * Adds the specified file to this request, and returns the uodated request object
     *
     * @param name The name of the file parameter
     * @param file The file
     * @return The updated request
     */
    Request<T> withMultiPartFile(String name, File file);

    /**
     * Adds the specified file to this request
     *
     * @param name The name of the file parameter
     * @param file The file
     */
    void addMultiPartFile(String name, File file);

    /**
     * Adds the specified file to this request
     *
     * @param name The name of the file parameter
     * @param in   the file
     */
    void addMultiPartFile(String name, InputStream in);

    /**
     * Returns the service endpoint to which this request should be sent.
     *
     * @return The service endpoint to which this request should be sent.
     */
    URI getEndpoint();

    /**
     * Sets the service endpoint to which this equest should be sent.
     *
     * @param endpoint The service endpoint to which this request should be sent.
     */
    void setEndpoint(URI endpoint);

    /**
     * Sets the HTTP method (GET, POST, etc) to use when sending this request.
     *
     * @param httpMethod The HTTP method to use when sending this request.
     */
    void setHttpMethod(HttpMethodName httpMethod);

    /**
     * Returns the HTTP method (GET, POST, etc) to use when sending this
     * request.
     *
     * @return The HTTP method to use when sending this request.
     */
    HttpMethodName getHttpMethod();

    /**
     * Returns the optional value for time offset for this request.  This
     * will be used by the signer to adjust for potential clock skew.
     * Value is in seconds, positive values imply the current clock is "fast",
     * negative values imply clock is slow.
     *
     * @return The optional value for time offset (in seconds) for this request.
     */
    int getTimeOffset();

    /**
     * Sets the optional value for time offset for this request.  This
     * will be used by the signer to adjust for potential clock skew.
     * Value is in seconds, positive values imply the current clock is "fast",
     * negative values imply clock is slow.
     *
     * @param timeOffset The optional value for time offset (in seconds) for this request.
     */
    void setTimeOffset(int timeOffset);


    /**
     * Sets the optional value for time offset for this request.  This
     * will be used by the signer to adjust for potential clock skew.
     * Value is in seconds, positive values imply the current clock is "fast",
     * negative values imply clock is slow.
     *
     * @param timeOffset The optional value for time offset (in seconds) for this request.
     * @return The updated request object.
     */
    Request<T> withTimeOffset(int timeOffset);

    /**
     * Returns the optional stream containing the payload data to include for
     * this request. Not all requests will contain payload data.
     *
     * @return The optional stream containing the payload data to include for
     * this request.
     */
    InputStream getContent();

    /**
     * Sets the optional stream containing the payload data to include for this
     * request. This is used, for example, for S3 chunk encoding.
     *
     * @param content The optional stream containing the payload data to include for
     *                this request.
     */
    void setContent(InputStream content);

    /**
     * @return The name of the Yop service this request is for. This is used
     * as the service name set in request metrics and service
     * exceptions.
     */
    String getServiceName();

    /**
     * Returns the original, user facing request object which this internal
     * request object is representing.
     *
     * @return an instance of request as an <code>T</code>.
     */
    T getOriginalRequestObject();

    /**
     * @return whether this request is a yos request;
     */
    boolean isYosRequest();

    /**
     * assign this request to yos
     */
    void assignYos();

}
