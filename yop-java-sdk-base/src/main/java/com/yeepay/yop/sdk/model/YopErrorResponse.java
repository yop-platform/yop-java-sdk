package com.yeepay.yop.sdk.model;

public class YopErrorResponse {

    private String requestId;

    /**
     * The YOP error code which represents the error type.
     */
    private String code;

    /**
     * The detail error message.
     */
    private String message;

    /**
     * The YOP sub error code which represents the detail error type.
     */
    private String subCode;

    /**
     * The detail sub error message.
     */
    private String subMessage;

    private String docUrl;

    public String getDocUrl() {
        return docUrl;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Returns the YOP error code which represents the error type.
     *
     * @return the YOP error code which represents the error type.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Sets the YOP error code which represents the error type.
     *
     * @param code the YOP error code which represents the error type.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Returns the detail error message.
     *
     * @return the detail error message.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets the detail error message.
     *
     * @param message the detail error message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String getSubMessage() {
        return subMessage;
    }

    public void setSubMessage(String subMessage) {
        this.subMessage = subMessage;
    }
}
