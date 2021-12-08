package com.yeepay.yop.sdk;

/**
 * The YOP error code.
 */
public enum ErrorCode {
    ACCESS_DENIED("AccessDenied"),
    INAPPROPRIATE_JSON("InappropriateJSON"),
    INTERNAL_ERROR("InternalError"),
    INVALID_APP_KEY("InvalidAppKey"),
    INVALID_HTTP_AUTH_HEADER("InvalidHTTPAuthHeader"),
    INVALID_HTTP_REQUEST("InvalidHTTPRequest"),
    INVALID_URI("InvalidURI"),
    MALFORMED_JSON("MalformedJSON"),
    INVALID_VERSION("InvalidVersion"),
    OPT_IN_REQUIRED("OptInRequired"),
    PRECONDITION_FAILED("PreconditionFailed"),
    REQUEST_EXPIRED("RequestExpired"),
    SIGNATURE_DOES_NOT_MATCH("SignatureDoesNotMatch");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }

    public boolean equals(String code) {
        return this.code.equals(code);
    }
}
