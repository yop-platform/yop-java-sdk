package com.yeepay.yop.sdk.http.analyzer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.http.HttpResponseAnalyzer;
import com.yeepay.yop.sdk.http.HttpResponseHandleContext;
import com.yeepay.yop.sdk.internal.MultiPartFile;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.yos.BaseYosUploadResponse;
import com.yeepay.yop.sdk.model.yos.YosUploadResponseMetadata;
import com.yeepay.yop.sdk.utils.checksum.CRC64Utils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.zip.CheckedInputStream;

/**
 * title: 文件上传完整性校验<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-04-30 15:04
 */
public class YosUploadIntegrityCheckAnalyzer implements HttpResponseAnalyzer {

    private static final YosUploadIntegrityCheckAnalyzer INSTANCE = new YosUploadIntegrityCheckAnalyzer();

    public static YosUploadIntegrityCheckAnalyzer getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        if (!(response instanceof BaseYosUploadResponse)) {
            return false;
        }
        YosUploadResponseMetadata uploadResponseMetadata = ((BaseYosUploadResponse) response).getMetadata();
        if (uploadResponseMetadata.getCrc64ECMA() == null) {
            return false;
        }
        String requestCrc64 = getRequestCrc64(context.getOriginRequest());
        if (requestCrc64 == null) {
            return false;
        }
        if (StringUtils.equals(uploadResponseMetadata.getCrc64ECMA(), requestCrc64)) {
            return false;
        }
        YopServiceException ex = new YopServiceException("业务处理失败");
        ex.setErrorCode("40044");
        ex.setSubErrorCode("isv.scene.filestore.put.crc-failed");
        ex.setSubMessage("文件上传crc校验失败");
        throw ex;
    }

    private String getRequestCrc64(Request originRequest) {
        Map<String, List<MultiPartFile>> files = originRequest.getMultiPartFiles();
        if (MapUtils.isEmpty(files)) {
            return null;
        }
        Map<String, List<MultiPartFile>> sortedFiles = Maps.newTreeMap();
        sortedFiles.putAll(files);
        List<CheckedInputStream> inputStreams = Lists.newArrayListWithExpectedSize(sortedFiles.size());
        for (List<MultiPartFile> items : sortedFiles.values()) {
            for (MultiPartFile item : items) {
                inputStreams.add(item.getInputStream());
            }
        }
        return CRC64Utils.getCRC64(inputStreams);
    }
}
