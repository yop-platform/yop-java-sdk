/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.g3.sdk.yop.utils.time;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * title: 仅适用于序列化当前时间时，较FastDateFormat有性能提升<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2018/9/6 上午11:16
 */
public class CachingDateFormatter {

    private FastDateFormat fastDateFormat;

    private AtomicReference<CachedTime> cachedTime;

    private boolean onSecond;// 根据时间格式，决定缓存在秒级还是毫秒级

    public CachingDateFormatter(String pattern) {
        this(FastDateFormat.getInstance(pattern));
    }

    public CachingDateFormatter(FastDateFormat fastDateFormat) {
        this.fastDateFormat = fastDateFormat;
        onSecond = !fastDateFormat.getPattern().contains("SSS");

        long current = System.currentTimeMillis();
        this.cachedTime = new AtomicReference<CachedTime>(new CachedTime(current, fastDateFormat.format(current)));
    }

    public String format(final Date date) {
        return format(date.getTime());
    }

    public String format(final long timestampMillis) {
        long timestamp = onSecond ? timestampMillis / 1000 : timestampMillis;

        CachedTime cached = cachedTime.get();
        if (timestamp != cached.timestamp) {
            final CachedTime newCachedTime = new CachedTime(timestamp, fastDateFormat.format(timestampMillis));
            if (cachedTime.compareAndSet(cached, newCachedTime)) {
                cached = newCachedTime;
            } else {
                cached = cachedTime.get();
            }
        }

        return cached.formatted;
    }

    static final class CachedTime {
        public long timestamp;
        public String formatted;

        public CachedTime(final long timestamp, String formatted) {
            this.timestamp = timestamp;
            this.formatted = formatted;
        }
    }

}
