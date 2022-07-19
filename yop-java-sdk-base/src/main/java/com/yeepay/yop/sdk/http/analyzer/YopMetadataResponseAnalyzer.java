package com.yeepay.yop.sdk.http.analyzer;

import com.google.common.base.CharMatcher;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.HttpResponseAnalyzer;
import com.yeepay.yop.sdk.http.HttpResponseHandleContext;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopResponseMetadata;
import com.yeepay.yop.sdk.utils.X509CertUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: YopMetadataResponseAnalyzer<br>
 * description: HTTP response handler for YOP responses. Provides common utilities that other specialized YOP response
 * handlers need to share such as pulling common response metadata (ex: request IDs) out of headers.<br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/29 17:54
 */
public class YopMetadataResponseAnalyzer implements HttpResponseAnalyzer {

    private static final YopMetadataResponseAnalyzer INSTANCE = new YopMetadataResponseAnalyzer();

    public static YopMetadataResponseAnalyzer getInstance() {
        return INSTANCE;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(YopMetadataResponseAnalyzer.class);

    private YopMetadataResponseAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        YopHttpResponse httpResponse = context.getResponse();
        YopResponseMetadata metadata = response.getMetadata();
        metadata.setYopRequestId(httpResponse.getHeader(Headers.YOP_REQUEST_ID));
        metadata.setYopContentSha256(httpResponse.getHeader(Headers.YOP_CONTENT_SHA256));
        metadata.setYopSign(httpResponse.getHeader(Headers.YOP_SIGN));
        metadata.setYopVia(httpResponse.getHeader(Headers.YOP_VIA));
        metadata.setContentDisposition(httpResponse.getHeader(Headers.CONTENT_DISPOSITION));
        metadata.setContentEncoding(httpResponse.getHeader(Headers.CONTENT_ENCODING));
        metadata.setContentLength(httpResponse.getHeaderAsLong(Headers.CONTENT_LENGTH));
        metadata.setContentMd5(httpResponse.getHeader(Headers.CONTENT_MD5));
        metadata.setContentRange(httpResponse.getHeader(Headers.CONTENT_RANGE));
        metadata.setContentType(httpResponse.getHeader(Headers.CONTENT_TYPE));
        metadata.setDate(httpResponse.getHeaderAsRfc822Date(Headers.DATE));
        metadata.setTransferEncoding(httpResponse.getHeader(Headers.TRANSFER_ENCODING));
        String eTag = httpResponse.getHeader(Headers.ETAG);
        if (eTag != null) {
            metadata.setETag(CharMatcher.is('"').trimFrom(eTag));
        }
        metadata.setExpires(httpResponse.getHeaderAsRfc822Date(Headers.EXPIRES));
        metadata.setLastModified(httpResponse.getHeaderAsRfc822Date(Headers.LAST_MODIFIED));
        metadata.setServer(httpResponse.getHeader(Headers.SERVER));
        final String certSerialNo = httpResponse.getHeader(Headers.YOP_SIGN_CERT_SERIAL_NO);
        metadata.setYopCertSerialNo(X509CertUtils.parseToHex(
                StringUtils.defaultIfBlank(certSerialNo,
                        httpResponse.getHeader(Headers.YOP_CERT_SERIAL_NO))));
        metadata.setYopEncrypt(httpResponse.getHeader(Headers.YOP_ENCRYPT));
        handleYopResponseMetadata(metadata);
        return false;
    }

    private void handleYopResponseMetadata(YopResponseMetadata metadata) {
        if (StringUtils.equals(metadata.getYopVia(), YopConstants.DEFAULT_SANDBOX_VIA)) {
            LOGGER.info("response from sandbox-gateway");
        }
    }
}
