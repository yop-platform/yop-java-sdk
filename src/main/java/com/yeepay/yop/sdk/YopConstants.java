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

    String VERSION = "3.3.10";

    String DEFAULT_ENCODING = "UTF-8";

    Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

    String DEFAULT_SERVER_ROOT = "https://openapi.yeepay.com/yop-center";

    String DEFAULT_YOS_SERVER_ROOT = "https://yos.yeepay.com/yop-center";

    String DEFAULT_SANDBOX_SERVER_ROOT = "https://sandbox.yeepay.com/yop-center";

    List<URI> DEFAULT_PREFERRED_SERVER_ROOT = Lists.newArrayList(URI.create("https://openapi-a.yeepay.com/yop-center"), URI.create("https://openapi-h.yeepay.com/yop-center"));

    String DEFAULT_SANDBOX_VIA = "sandbox";

    /**
     * 易宝默认公钥
     */
    String YOP_RSA2048_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4g7dPL+CBeuzFmARI2GFjZpKODUROaMG+E6wdNfv5lhPqC3jjTIeljWU8AiruZLGRhl92QWcTjb3XonjaV6k9rf9adQtyv2FLS7bl2Vz2WgjJ0FJ5/qMaoXaT+oAgWFk2GypyvoIZsscsGpUStm6BxpWZpbPrGJR0N95un/130cQI9VCmfvgkkCaXt7TU1BbiYzkc8MDpLScGm/GUCB2wB5PclvOxvf5BR/zNVYywTEFmw2Jo0hIPPSWB5Yyf2mx950Fx8da56co/FxLdMwkDOO51Qg3fbaExQDVzTm8Odi++wVJEP1y34tlmpwFUVbAKIEbyyELmi/2S6GG0j9vNwIDAQAB";

    String YOP_SESSION_ID = UUID.randomUUID().toString();

    String REPORT_API_URI = "/rest/v1.0/yop/client/report", REPORT_API_METHOD = "POST";

    String YOP_HTTP_CONTENT_TYPE_JSON = "application/json";

    String HEADER_LANG_JAVA = "java";

}
