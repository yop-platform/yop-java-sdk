package com.yeepay.yop.sdk.exception;

/**
 * Base exception class for any errors that occur on the client side when attempting to access a YOP service API.
 * <p>
 * <p>
 * For example, there is no network connection available or the network request is timeout, or the server returns an
 * invalid response that the client is unable to parse, etc
 * <p>
 * <p>
 * Error responses from services will be handled as YopServiceExceptions.
 *
 * @see YopServiceException
 */
public class YopClientException extends RuntimeException {
    private static final long serialVersionUID = -9085416005820812953L;

    /**
     * Constructs a new YopClientException with the specified detail message.
     *
     * @param message the detail error message.
     */
    public YopClientException(String message) {
        super(message);
    }

    /**
     * Constructs a new YopClientException with the specified detail message and the underlying cause.
     *
     * @param message the detail error message.
     * @param cause   the underlying cause of this exception.
     */
    public YopClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
