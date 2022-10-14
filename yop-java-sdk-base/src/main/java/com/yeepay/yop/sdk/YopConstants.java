/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk;

import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.constants.CharacterConstants;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

/**
 * Common constants used by the whole SDK.
 */
public interface YopConstants {

    String VERSION = "4.3.3";

    String DEFAULT_ENCODING = "UTF-8";

    Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

    String DEFAULT_SERVER_ROOT = "https://openapi.yeepay.com/yop-center";

    String DEFAULT_YOS_SERVER_ROOT = "https://yos.yeepay.com/yop-center";

    String DEFAULT_SANDBOX_SERVER_ROOT = "https://sandbox.yeepay.com/yop-center";

    String DEFAULT_SANDBOX_VIA = "sandbox";

    String DEFAULT_YOP_PROTOCOL_VERSION = "yop-auth-v3";

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
    String YOP_HTTP_CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data";
    String YOP_HTTP_CONTENT_TYPE_STREAM = "application/octet-stream";
    String YOP_HTTP_CONTENT_TYPE_TEXT = "text/plain;charset=UTF-8";

    String YOP_DEFAULT_APPKEY = "default";

    /**
     * 加密
     */
    String YOP_ENCRYPT_V1 = "yop-encrypt-v1";
    String SM4_CBC_PKCS5PADDING = "SM4/CBC/PKCS5Padding";
    String SM2 = "SM2";
    String YOP_DEFAULT_ENCRYPT_ALG = SM4_CBC_PKCS5PADDING;
    String YOP_CREDENTIALS_ENCRYPT_ALG_SM4 = SM4_CBC_PKCS5PADDING;
    String YOP_CREDENTIALS_ENCRYPT_ALG_SM2 = SM2;
    String YOP_CREDENTIALS_DEFAULT_ENCRYPT_ALG = YOP_CREDENTIALS_ENCRYPT_ALG_SM2;
    String YOP_ENCRYPT_OPTIONS_YOP_SM2_CERT_SERIAL_NO = "YOP_SM2_CERT_SERIAL_NO";
    String YOP_ENCRYPT_OPTIONS_YOP_SM4_MAIN_CREDENTIALS = "SM4_CERT_ID";
    String YOP_JSON_CONTENT_FORMAT = "{\"result\":%s}";
    String YOP_JSON_CONTENT_BIZ_KEY = "result";

    /**
     * jsonpath
     */
    String JSON_PATH_PREFIX = "$.";
    Set<String> JSON_PATH_ROOT = Collections.unmodifiableSet(Sets.newHashSet("$", "$..*"));
    Set<String> TOTAL_ENCRYPT_PARAMS = Sets.newHashSet(CharacterConstants.DOLLAR);

    /**
     * SDK内置证书
     */
    String DEFAULT_CERT_PATH = "config/certs";
    String DEFAULT_CFCA_ROOT_FILE = "cfca_root.pem";
    String DEFAULT_YOP_INTER_FILE = "yop_inter.pem";
    YopCertStore DEFAULT_LOCAL_YOP_CERT_STORE = new YopCertStore(DEFAULT_CERT_PATH);
    // 默认过期后24小时内可用
    long DEFAULT_PERIOD_VALID_AFTER_EXPIRE = 24 * 3600 * 1000;

    // 默认过期前72小时内开始刷新
    long DEFAULT_PERIOD_REFRESH_BEFORE_EXPIRE = 72 * 3600 * 1000;

    // 商户通知
    String SM4_CALLBACK_ALGORITHM = "AEAD_SM4_GCM";
    String DEFAULT_YOP_CALLBACK_HANDLER = "default";

}
