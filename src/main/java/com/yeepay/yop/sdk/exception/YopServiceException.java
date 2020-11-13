package com.yeepay.yop.sdk.exception;

/**
 * Extension of YopClientException that represents an error response returned by a YOP service.
 * Receiving an exception of this type indicates that the caller's request was correctly transmitted to the service,
 * but for some reason, the service was not able to process it, and returned an error response instead.
 * <p>
 * <p>
 * YopServiceException provides callers several pieces of information that can be used to obtain more information
 * about the error and why it occurred. In particular, the errorType field can be used to determine if the caller's
 * request was invalid, or the service encountered an error on the server side while processing it.
 */
public class YopServiceException extends YopClientException {
    private static final long serialVersionUID = 1483785729559154396L;

    /**
     * Indicates who is responsible (if known) for a failed request.
     */
    public enum ErrorType {
        Client,
        Service,
        Unknown
    }

    /**
     * The unique YOP identifier for the service request the caller made. The YOP request ID can uniquely identify
     * the YOP request, and is used for reporting an error to YOP support team.
     */
    private String requestId;

    /**
     * The YOP error code represented by this exception.
     */
    private String errorCode;

    /**
     * The Yop sub error code represented by this exception
     */
    private String subErrorCode;

    /**
     * Indicates (if known) whether this exception was the fault of the caller or the service.
     */
    private ErrorType errorType = ErrorType.Unknown;

    /**
     * The error message as returned by the service.
     */
    private String errorMessage;

    /**
     * The detail error message as returned by the service
     */
    private String subMessage;

    /**
     * The HTTP status code that was returned with this error.
     */
    private int statusCode;

    private String docUrl;

    /**
     * Constructs a new YopServiceException with the specified message.
     *
     * @param errorMessage An error message describing what went wrong.
     */
    public YopServiceException(String errorMessage) {
        super(null);
        this.errorMessage = errorMessage;
    }

    /**
     * Constructs a new YopServiceException with the specified message and exception indicating the root cause.
     *
     * @param errorMessage An error message describing what went wrong.
     * @param cause        The root exception that caused this exception to be thrown.
     */
    public YopServiceException(String errorMessage, Exception cause) {
        super(null, cause);
        this.errorMessage = errorMessage;
    }

    /**
     * Sets the YOP requestId for this exception.
     *
     * @param requestId The unique identifier for the service request the caller made.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Returns the YOP request ID that uniquely identifies the service request the caller made.
     *
     * @return The YOP request ID that uniquely identifies the service request the caller made.
     */
    public String getRequestId() {
        return this.requestId;
    }

    /**
     * Sets the YOP error code represented by this exception.
     *
     * @param errorCode The YOP error code represented by this exception.
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Returns the YOP error code represented by this exception.
     *
     * @return The YOP error code represented by this exception.
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    public String getSubErrorCode() {
        return subErrorCode;
    }

    public void setSubErrorCode(String subErrorCode) {
        this.subErrorCode = subErrorCode;
    }

    /**
     * Sets the type of error represented by this exception (sender, receiver, or unknown),
     * indicating if this exception was the caller's fault, or the service's fault.
     *
     * @param errorType The type of error represented by this exception (sender or receiver),
     *                  indicating if this exception was the caller's fault or the service's fault.
     */
    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    /**
     * Indicates who is responsible for this exception (caller, service, or unknown).
     *
     * @return A value indicating who is responsible for this exception (caller, service, or unknown).
     */
    public ErrorType getErrorType() {
        return this.errorType;
    }

    /**
     * Sets the human-readable error message provided by the service.
     *
     * @param errorMessage the human-readable error message provided by the service.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the human-readable error message provided by the service.
     *
     * @return the human-readable error message provided by the service.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getSubMessage() {
        return subMessage;
    }

    public void setSubMessage(String subMessage) {
        this.subMessage = subMessage;
    }

    /**
     * Sets the HTTP status code that was returned with this service exception.
     *
     * @param statusCode The HTTP status code that was returned with this service exception.
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDocUrl() {
        return docUrl;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }


    /**
     * Returns the HTTP status code that was returned with this service exception.
     *
     * @return The HTTP status code that was returned with this service exception.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getMessage() {
        return this.getErrorMessage()
                + " (Status Code: " + this.getStatusCode()
                + "; Error Code: " + this.getErrorCode()
                + "; Sub Code: " + this.getSubErrorCode()
                + "; Sub Message: " + this.getSubMessage()
                + "; Request ID: " + this.getRequestId()
                + "; docUrl: " + this.getDocUrl()
                + ")";
    }
}
