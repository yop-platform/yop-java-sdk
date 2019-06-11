package com.yeepay.g3.sdk.yop.http;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 16/2/18 16:36
 */
public interface Headers {

    /**
     * 标准的 HTTP Headers
     */

    String AUTHORIZATION = "Authorization";

    String CONTENT_DISPOSITION = "Content-Disposition";

    String CONTENT_ENCODING = "Content-Encoding";

    String TRANSFER_ENCODING = "Transfer-Encoding";

    String CONTENT_LENGTH = "Content-Length";

    String CONTENT_MD5 = "Content-MD5";

    String CONTENT_RANGE = "Content-Range";

    String CONTENT_TYPE = "Content-Type";

    String DATE = "Date";

    String ETAG = "ETag";

    String EXPIRES = "Expires";

    String HOST = "Host";

    String LAST_MODIFIED = "Last-Modified";

    String RANGE = "Range";

    String SERVER = "Server";

    String USER_AGENT = "User-Agent";

    /**
     * YOP 通用 HTTP Headers
     */

    String YOP_PREFIX = "x-yop-";

    String YOP_ACL = "x-yop-acl";

    String YOP_CONTENT_SHA256 = "x-yop-content-sha256";

    String YOP_COPY_METADATA_DIRECTIVE = "x-yop-metadata-directive";

    String YOP_COPY_SOURCE = "x-yop-copy-source";

    String YOP_COPY_SOURCE_IF_MATCH = "x-yop-copy-source-if-match";

    String YOP_APP_KEY = "x-yop-appkey";
    String YOP_SUB_CUSTOMER_ID = "x-yop-sub-customer-id";

    String YOP_DATE = "x-yop-date";

    String YOP_USER_METADATA_PREFIX = "x-yop-meta-";

    String YOP_REQUEST_ID = "x-yop-request-id";

    String YOP_SESSION_ID = "x-yop-session-id";

    String YOP_SDK_VERSION = "x-yop-sdk-version";

    String YOP_SDK_LANGS = "x-yop-sdk-langs";

    String YOP_SECURE_TOKEN = "x-yop-secure-token";

    /*请求来源*/
    String YOP_REQUEST_SOURCE = "x-yop-request-source";

    /**
     * 文件校验头
     */
    String YOP_HASH_CRC64ECMA = "x-yop-hash-crc64ecma";

    String YOP_VIA = "x-yop-via";

    /**
     * 加密类型
     */
    String YOP_ENCRYPT_TYPE = "x-yop-encrypt-type";

    /**
     * 签名
     */
    String YOP_SIGN = "x-yop-sign";

}
