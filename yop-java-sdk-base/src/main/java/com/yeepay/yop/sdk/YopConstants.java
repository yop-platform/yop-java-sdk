/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk;

import com.google.common.collect.Lists;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

/**
 * Common constants used by the whole SDK.
 */
public interface YopConstants {

    String VERSION = "4.2.4";

    String DEFAULT_ENCODING = "UTF-8";

    Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

    String DEFAULT_SERVER_ROOT = "https://openapi.yeepay.com/yop-center";

    String DEFAULT_YOS_SERVER_ROOT = "https://yos.yeepay.com/yop-center";

    String DEFAULT_SANDBOX_SERVER_ROOT = "https://sandbox.yeepay.com/yop-center";

    List<URI> DEFAULT_PREFERRED_SERVER_ROOT = Lists.newArrayList(URI.create("https://openapi-a.yeepay.com/yop-center"), URI.create("https://openapi-h.yeepay.com/yop-center"));

    String DEFAULT_SANDBOX_VIA = "sandbox";

    String DEFAULT_YOP_CERT_STORE_PATH = "/tmp/yop/certs";

    String SM2_PROTOCOL_PREFIX = "YOP-SM2-SM3";

    String YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO = "rsa";
    String YOP_SM_PLATFORM_CERT_DEFAULT_SERIAL_NO = "sm";

    String YOP_RSA_PLATFORM_CERT_PREFIX = "yop_platform_rsa_cert_";
    String YOP_SM_PLATFORM_CERT_PREFIX = "yop_platform_sm_cert_";
    String YOP_PLATFORM_CERT_POSTFIX = ".cer";

    String ISV_ENCRYPT_KEY = "ISV_ENCRYPT_KEY";

    /**
     * 易宝默认公钥
     */
    String YOP_RSA2048_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4g7dPL+CBeuzFmARI2GFjZpKODUROaMG+E6wdNfv5lhPqC3jjTIeljWU8AiruZLGRhl92QWcTjb3XonjaV6k9rf9adQtyv2FLS7bl2Vz2WgjJ0FJ5/qMaoXaT+oAgWFk2GypyvoIZsscsGpUStm6BxpWZpbPrGJR0N95un/130cQI9VCmfvgkkCaXt7TU1BbiYzkc8MDpLScGm/GUCB2wB5PclvOxvf5BR/zNVYywTEFmw2Jo0hIPPSWB5Yyf2mx950Fx8da56co/FxLdMwkDOO51Qg3fbaExQDVzTm8Odi++wVJEP1y34tlmpwFUVbAKIEbyyELmi/2S6GG0j9vNwIDAQAB";

    String TLS_VERSION_1_1 = "TLSv1.1";
    String TLS_VERSION_1_2 = "TLSv1.2";

    String JDK_VERSION = "java.version";
    String JDK_VERSION_1_6 = "1.6";
    String JDK_VERSION_1_7 = "1.7";
    String JDK_VERSION_1_8 = "1.8";

    String FILE_PROTOCOL_PREFIX = "file://";

    String RSA_ECB_PKCS1PADDING = "RSA/ECB/PKCS1Padding";

    String YOP_HTTP_CLIENT_IMPL_OK = "ok";
    String YOP_HTTP_CLIENT_IMPL_APACHE = "apache";
    String YOP_HTTP_CLIENT_IMPL_DEFAULT = YOP_HTTP_CLIENT_IMPL_APACHE;

    String YOP_HTTP_CONTENT_TYPE_JSON = "application/json";
    String YOP_HTTP_CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    String YOP_HTTP_CONTENT_TYPE_STREAM = "application/octet-stream";

    String YOP_DEFAULT_APPKEY = "default";

    String YOP_SESSION_ID = UUID.randomUUID().toString();

    String REPORT_API_URI = "/rest/v1.0/yop/client/report", REPORT_API_METHOD = "POST";

    String HEADER_LANG_JAVA = "java";

}
