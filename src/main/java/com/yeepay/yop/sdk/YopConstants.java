package com.yeepay.yop.sdk;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.charset.Charset;

/**
 * Common constants used by the whole SDK.
 */
public interface YopConstants {

    String VERSION = "3.3.2";

    String DEFAULT_ENCODING = "UTF-8";

    Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

    String DEFAULT_SERVER_ROOT = "https://openapi.yeepay.com/yop-center";

    String DEFAULT_YOS_SERVER_ROOT = "https://yos.yeepay.com/yop-center";

    String DEFAULT_SANDBOX_SERVER_ROOT = "https://sandbox.yeepay.com/yop-center";

    String DEFAULT_SANDBOX_VIA = "sandbox";

    DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

}
