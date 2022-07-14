package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptProtocol;

/**
 * Common YOS HTTP header values used throughout the YOP YOS Java client.
 */
public interface Headers {

    /*
     * Standard HTTP Headers
     */

    String YOP_APP_KEY = "x-yop-appkey";

    String YOP_SUB_CUSTOMER_ID = "x-yop-sub-customer-id";

    String YOP_SESSION_ID = "x-yop-session-id";

    String YOP_SDK_VERSION = "x-yop-sdk-version";

    String YOP_REQUEST_SOURCE = "x-yop-request-source";

    String AUTHORIZATION = "Authorization";

    String CACHE_CONTROL = "Cache-Control";

    String CONTENT_DISPOSITION = "Content-Disposition";

    String CONTENT_ENCODING = "Content-Encoding";

    String CONTENT_LENGTH = "Content-Length";

    String CONTENT_MD5 = "Content-MD5";

    String CONTENT_RANGE = "Content-Range";

    String CONTENT_TYPE = "Content-Type";

    String DATE = "Date";

    String ETAG = "ETag";

    String EXPIRES = "Expires";

    String HOST = "Host";

    String LAST_MODIFIED = "Last-Modified";

    String LOCATION = "Location";

    String RANGE = "Range";

    String SERVER = "Server";

    String TRANSFER_ENCODING = "Transfer-Encoding";

    String USER_AGENT = "User-Agent";


    /*
     * YOP Common HTTP Headers
     */

    String YOP_ACL = "x-yop-acl";

    String YOP_CONTENT_SHA256 = "x-yop-content-sha256";

    String YOP_CONTENT_SM3 = "x-yop-content-sm3";

    /**
     * 签名
     */
    String YOP_SIGN = "x-yop-sign";

    String YOP_HASH_CRC64ECMA = "x-yop-hash-crc64ecma";

    String YOP_COPY_METADATA_DIRECTIVE = "x-yop-metadata-directive";

    String YOP_COPY_SOURCE_IF_MATCH = "x-yop-copy-source-if-match";

    String YOP_DATE = "x-yop-date";

    String YOP_APPKEY = "x-yop-appkey";

    String YOP_PREFIX = "x-yop-";

    String YOP_REQUEST_ID = "x-yop-request-id";

    String YOP_SECURITY_TOKEN = "x-yop-security-token";

    String YOP_USER_METADATA_PREFIX = "x-yop-meta-";

    String YOP_VIA = "x-yop-via";

    /**
     * 加密协议头
     *
     * @see YopEncryptProtocol#YOP_ENCRYPT_PROTOCOL_V1_REQ
     */
    String YOP_ENCRYPT = "x-yop-encrypt";

    /*
     * YOS HTTP Headers
     */

    String YOP_COPY_SOURCE = "x-yop-copy-source";

    String YOP_COPY_SOURCE_IF_MODIFIED_SINCE = "x-yop-copy-source-if-modified-since";

    String YOP_COPY_SOURCE_IF_NONE_MATCH = "x-yop-copy-source-if-none-match";

    String YOP_COPY_SOURCE_IF_UNMODIFIED_SINCE = "x-yop-copy-source-if-unmodified-since";

    String YOP_DEBUG_ID = "x-yop-debug-id";

    String YOP_NEXT_APPEND_OFFSET = "x-yop-next-append-offset";

    String YOP_OBJECT_TYPE = "x-yop-object-type";

    String YOP_CERT_SERIAL_NO = "x-yop-serial-no";

    String YOP_SIGN_CERT_SERIAL_NO = "x-yop-sign-serial-no";

}
