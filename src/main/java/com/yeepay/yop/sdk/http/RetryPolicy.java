package com.yeepay.yop.sdk.http;

import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;

/**
 * Retry policy that can be configured on a specific service client using {@link com.yeepay.g3.core.yop.sdk.sample.client.ClientConfiguration}.
 */
public interface RetryPolicy {

    /**
     * SDK default max retry count.
     */
    public static final int DEFAULT_MAX_ERROR_RETRY = 1;
    /**
     * Maximum exponential back-off time before retrying a request.
     */
    public static final int DEFAULT_MAX_DELAY_IN_MILLIS = 20 * 1000;
    /**
     * SDK default retry policy.
     */
    public static final DefaultRetryPolicy DEFAULT_RETRY_POLICY = new DefaultRetryPolicy();

    /**
     * Returns the maximum number of retry attempts.
     *
     * @return The maximum number of retry attempts.
     */
    public int getMaxErrorRetry();

    /**
     * Returns the maximum delay time (in milliseconds) before retrying a request.
     *
     * @return the maximum delay time (in milliseconds) before retrying a request.
     */
    public long getMaxDelayInMillis();

    /**
     * Returns the delay (in milliseconds) before next retry attempt. A negative value indicates that no more retries
     * should be made.
     *
     * @param exception        the exception from the failed request, represented as an YopClientException object.
     * @param retriesAttempted the number of times the current request has been attempted
     *                         (not including the next attempt after the delay).
     * @return the delay (in milliseconds) before next retry attempt.A negative value indicates that no more retries
     * should be made.
     */
    public long getDelayBeforeNextRetryInMillis(YopClientException exception, int retriesAttempted);
}
