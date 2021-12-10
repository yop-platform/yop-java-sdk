package com.yeepay.yop.sdk.http;


import com.yeepay.yop.sdk.http.analyzer.*;

/**
 * title:HttpResponseAnalyzer工厂类 <br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/29 18:57
 */
public class HttpResponseAnalyzerSupport {

    private static final HttpResponseAnalyzer[] ANALYZER_CHAIN = new HttpResponseAnalyzer[]{
            YopMetadataResponseAnalyzer.getInstance(),
            YopSignatureCheckAnalyzer.getInstance(),
            YopContentDecryptAnalyzer.getInstance(),
            YopErrorResponseAnalyzer.getInstance(),
            YopJsonResponseAnalyzer.getInstance()
    };

    private static final HttpResponseAnalyzer[] YOS_DOWNLOAD_ANALYZER_CHAIN = new HttpResponseAnalyzer[]{
            YopMetadataResponseAnalyzer.getInstance(),
            YopSignatureCheckAnalyzer.getInstance(),
            YopContentDecryptAnalyzer.getInstance(),
            YopErrorResponseAnalyzer.getInstance(),
            YosDownloadResponseAnalyzer.getInstance()
    };

    private static final HttpResponseAnalyzer[] YOS_UPLOAD_ANALYZER_CHAIN = new HttpResponseAnalyzer[]{
            YopMetadataResponseAnalyzer.getInstance(),
            YosUploadResponseMetadataAnalyzer.getInstance(),
            YosUploadIntegrityCheckAnalyzer.getInstance(),
            YopSignatureCheckAnalyzer.getInstance(),
            YopContentDecryptAnalyzer.getInstance(),
            YopErrorResponseAnalyzer.getInstance(),
            YopJsonResponseAnalyzer.getInstance()
    };

    public static HttpResponseAnalyzer[] getAnalyzerChain() {
        return ANALYZER_CHAIN;
    }

    public static HttpResponseAnalyzer[] getYosDownloadAnalyzerChain() {
        return YOS_DOWNLOAD_ANALYZER_CHAIN;
    }

    public static HttpResponseAnalyzer[] getYosUploadAnalyzerChain() {
        return YOS_UPLOAD_ANALYZER_CHAIN;
    }
}
