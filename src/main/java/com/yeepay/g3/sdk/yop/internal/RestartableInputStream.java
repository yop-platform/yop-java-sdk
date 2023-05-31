package com.yeepay.g3.sdk.yop.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Abstract class Restartable InputStream extends InputStream.
 */
public abstract class RestartableInputStream extends InputStream {
    public abstract void restart();

    public static RestartableInputStream wrap(byte[] b) {
        ByteArrayInputStream input = new ByteArrayInputStream(b);
        input.mark(b.length);
        return new RestartableResettableInputStream(input);
    }
}
