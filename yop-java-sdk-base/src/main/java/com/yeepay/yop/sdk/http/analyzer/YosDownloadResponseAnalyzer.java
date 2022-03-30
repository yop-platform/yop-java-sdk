package com.yeepay.yop.sdk.http.analyzer;

import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.HttpResponseHandleContext;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.http.HttpResponseAnalyzer;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.yos.YosDownloadInputStream;
import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import com.yeepay.yop.sdk.model.yos.YosDownloadResponseMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: <br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/3/28 16:00
 */
public class YosDownloadResponseAnalyzer implements HttpResponseAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(YosDownloadResponseAnalyzer.class);

    private static final YosDownloadResponseAnalyzer INSTANCE = new YosDownloadResponseAnalyzer();

    public static YosDownloadResponseAnalyzer getInstance() {
        return INSTANCE;
    }

    private YosDownloadResponseAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        if (!(response instanceof YosDownloadResponse)) {
            return false;
        }
        YopHttpResponse httpResponse = context.getResponse();
        YosDownloadResponse downloadResponse = (YosDownloadResponse) response;
        YosDownloadResponseMetadata downloadMetadata = downloadResponse.getMetadata();
        downloadMetadata.setAppendOffset(httpResponse.getHeaderAsLong(Headers.YOP_NEXT_APPEND_OFFSET));
        downloadMetadata.setCacheControl(httpResponse.getHeader(Headers.CACHE_CONTROL));

        if (downloadMetadata.getContentRange() != null) {
            int pos = downloadMetadata.getContentRange().lastIndexOf('/');
            if (pos >= 0) {
                try {
                    downloadMetadata.setInstanceLength(Long.parseLong(downloadMetadata.getContentRange().substring(pos + 1)));
                } catch (NumberFormatException e) {
                    logger.warn("Fail to parse length from " + Headers.CONTENT_RANGE + ": " + downloadMetadata.getContentRange(), e);
                }
            }
        }
        downloadMetadata.setLastModified(httpResponse.getHeaderAsRfc822Date(Headers.LAST_MODIFIED));
        downloadMetadata.setYopContentSha256(httpResponse.getHeader(Headers.YOP_CONTENT_SHA256));

        downloadResponse.setResult(new YosDownloadInputStream(httpResponse));
        return true;
    }
}
