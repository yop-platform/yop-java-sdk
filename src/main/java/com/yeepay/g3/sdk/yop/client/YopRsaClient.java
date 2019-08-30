package com.yeepay.g3.sdk.yop.client;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.yeepay.g3.sdk.yop.encrypt.Base64;
import com.yeepay.g3.sdk.yop.encrypt.*;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.http.HttpMethodName;
import com.yeepay.g3.sdk.yop.http.HttpUtils;
import com.yeepay.g3.sdk.yop.model.PartETag;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.*;
import com.yeepay.g3.sdk.yop.utils.checksum.CRC64Utils;
import com.yeepay.g3.sdk.yop.utils.io.IOUtil;
import com.yeepay.g3.sdk.yop.utils.io.MarkableFileInputStream;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.zip.CheckedInputStream;

/**
 * <pre>
 * 非对称 Client，简化用户发起请求及解析结果的处理
 * </pre>
 *
 * @author baitao.ji
 * @version 3.0
 */
public class YopRsaClient extends AbstractClient {

    protected static final Logger LOGGER = LoggerFactory.getLogger(YopRsaClient.class);

    private static final Set<String> defaultHeadersToSign = Sets.newHashSet();
    private static final Joiner headerJoiner = Joiner.on('\n');
    private static final Joiner signedHeaderStringJoiner = Joiner.on(';');

    private static final String EXPIRED_SECONDS = "1800";

    static {
//        defaultHeadersToSign.add(Headers.HOST.toLowerCase());
//        defaultHeadersToSign.add(Headers.CONTENT_LENGTH.toLowerCase());
//        defaultHeadersToSign.add(Headers.CONTENT_TYPE.toLowerCase());
//        defaultHeadersToSign.add(Headers.CONTENT_MD5.toLowerCase());
//        defaultHeadersToSign.add(Headers.YOP_HASH_CRC64ECMA.toLowerCase());
    }

    public static YopResponse get(String apiUri, YopRequest request) throws IOException {
        return handleNormalRequest(apiUri, request, HttpMethodName.GET);
    }

    /**
     * 发起post请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址或命名模式的method
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse post(String apiUri, YopRequest request) throws IOException {
        return handleNormalRequest(apiUri, request, HttpMethodName.POST);
    }

    private static YopResponse handleNormalRequest(String apiUri, YopRequest request, HttpMethodName method) throws IOException {
        CheckUtils.checkApiUri(apiUri);
        if (BooleanUtils.isTrue(request.isNeedEncrypt())) {
            encryptRequest(request);
        }
        sign(apiUri, request, method);
        String contentUrl = richRequest(apiUri, request);
        HttpUriRequest httpPost = buildFormHttpRequest(request, contentUrl, method);
        YopResponse response = fetchContentByApacheHttpClient(httpPost, new ResponseConfig()
                .withNeedEncrypt(request.isNeedEncrypt())
                .withEncryptKey(request.getEncryptKey())
                .withYopPublicKey(InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048)));
        handleRsaResult(response);
        return response;
    }

    private static void encryptRequest(YopRequest request) {
        String encryptKey = StringUtils.defaultIfBlank(request.getEncryptKey(), request.getAppSdkConfig().getEncryptKey());
        if (StringUtils.isBlank(encryptKey)) {
            throw new YopClientException("no encryptKey configured");
        }
        request.addHeader(Headers.YOP_ENCRYPT_TYPE, getEncryptType(encryptKey));
        //加密密钥回写，用于接下来构造ResponseConfig信息
        request.setEncryptKey(encryptKey);

        //参数值加密
        if (request.getParams() != null) {
            Multimap<String, String> paramMultiMap = request.getParams();
            for (String key : paramMultiMap.keySet()) {
                Collection<String> values = paramMultiMap.get(key);
                Collection<String> encryptedValues = new ArrayList<String>(values.size());
                for (String value : values) {
                    encryptedValues.add(AESEncrypter.encrypt(value, encryptKey));
                }
                paramMultiMap.replaceValues(key, encryptedValues);
            }
        }
    }

    private static String getEncryptType(String encryptKey) {
        byte[] decoded = Base64.decode(encryptKey.getBytes());
        if (decoded.length == 16 || decoded.length == 32) {
            return "aes" + decoded.length * 8;
        } else {
            throw new YopClientException("unsupported encryptKey length");
        }
    }

    /**
     * 上传文件
     *
     * @param apiUri  目标地址或命名模式的method
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse upload(String apiUri, YopRequest request) throws IOException {
        CheckUtils.checkApiUri(apiUri);
        if (BooleanUtils.isTrue(request.isNeedEncrypt())) {
            encryptRequest(request);
        }
        sign(apiUri, request, HttpMethodName.POST);
        String contentUrl = richRequest(apiUri, request);
        Pair<HttpUriRequest, List<CheckedInputStream>> pair = buildMultiFormRequest(request, contentUrl);
        YopResponse response = fetchContentByApacheHttpClient(pair.getLeft(), new ResponseConfig()
                .withNeedEncrypt(request.isNeedEncrypt())
                .withEncryptKey(request.getEncryptKey())
                .withYopPublicKey(InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048)));
        handleRsaResult(response);
        if (pair.getRight() != null) {
            checkFileIntegrity(response, CRC64Utils.getCRC64(pair.getRight()));
        }
        return response;
    }

    private static void sign(String apiUri, YopRequest request, HttpMethodName httpMethod) {
        String appKey = request.getAppSdkConfig().getAppKey();
        String timestamp = DateUtils.formatCompressedIso8601Timestamp(System.currentTimeMillis());

//        authorization  yop-auth-v2/openSmsApi/2016-02-25T08:57:48Z/1800/host/a57365cb4bf6cd83c91dfae214c1404aa0cc74f2ade95f121530fcb9c91f3c9d

        Map<String, String> headers = request.getHeaders();
        headers.put(Headers.YOP_SESSION_ID, SESSION_ID);
        headers.put(Headers.YOP_REQUEST_ID, getUUID());

        Set<String> headersToSignSet = new HashSet<String>();
        headersToSignSet.add(Headers.YOP_REQUEST_ID);

        String authString = InternalConfig.PROTOCOL_VERSION + "/" + appKey + "/" + timestamp + "/" + EXPIRED_SECONDS;

        // Formatting the URL with signing protocol.
        String canonicalURI = HttpUtils.getCanonicalURIPath(apiUri);
        // Formatting the query string with signing protocol.
        String canonicalQueryString = HttpUtils.getCanonicalQueryString(request.getParams(), true);
        // Sorted the headers should be signed from the request.
        SortedMap<String, String> headersToSign = getHeadersToSign(headers, headersToSignSet);
        // Formatting the headers from the request based on signing protocol.
        String canonicalHeader = getCanonicalHeaders(headersToSign);
        String signedHeaders = signedHeaderStringJoiner.join(headersToSign.keySet());
        signedHeaders = signedHeaders.trim().toLowerCase();

        String canonicalRequest = authString + "\n" + httpMethod + "\n" + canonicalURI + "\n" + canonicalQueryString + "\n" + canonicalHeader;

        // Signing the canonical request using key with sha-256 algorithm.

        PrivateKey isvPrivateKey;
        if (StringUtils.length(request.getSecretKey()) > 128) {
            try {
                isvPrivateKey = RSAKeyUtils.string2PrivateKey(request.getSecretKey());
            } catch (NoSuchAlgorithmException e) {
                throw Exceptions.unchecked(e);
            } catch (InvalidKeySpecException e) {
                throw Exceptions.unchecked(e);
            }
        } else {
            isvPrivateKey = request.getAppSdkConfig().getDefaultIsvPrivateKey();
        }
        if (null == isvPrivateKey) {
            throw new YopClientException("Can't init ISV private key!");
        }

        DigitalSignatureDTO digitalSignatureDTO = new DigitalSignatureDTO();
        digitalSignatureDTO.setPlainText(canonicalRequest);
        digitalSignatureDTO.setCertType(CertTypeEnum.RSA2048);
        digitalSignatureDTO.setDigestAlg(DigestAlgEnum.SHA256);
        digitalSignatureDTO = DigitalEnvelopeUtils.sign(digitalSignatureDTO, isvPrivateKey);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("canonicalRequest:" + canonicalRequest);
            LOGGER.debug("signature:" + digitalSignatureDTO.getSignature());
        }

        headers.put(Headers.AUTHORIZATION, "YOP-RSA2048-SHA256 " + InternalConfig.PROTOCOL_VERSION + "/" + appKey + "/" + timestamp + "/" + EXPIRED_SECONDS + "/" + signedHeaders + "/" + digitalSignatureDTO.getSignature());
    }

    private static void handleRsaResult(YopResponse response) {
        String stringResult = response.getStringResult();
        if (StringUtils.isNotBlank(stringResult)) {
            response.setResult(JacksonJsonMarshaller.unmarshal(stringResult, Object.class));
        }
    }

    private static String getCanonicalHeaders(SortedMap<String, String> headers) {
        if (headers.isEmpty()) {
            return "";
        }

        List<String> headerStrings = Lists.newArrayList();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            String value = entry.getValue();
            if (value == null) {
                value = "";
            }
            headerStrings.add(HttpUtils.normalize(key.trim().toLowerCase()) + ':' + HttpUtils.normalize(value.trim()));
        }
        Collections.sort(headerStrings);

        return headerJoiner.join(headerStrings);
    }

    private static SortedMap<String, String> getHeadersToSign(Map<String, String> headers, Set<String> headersToSign) {
        SortedMap<String, String> ret = Maps.newTreeMap();
        if (headersToSign != null) {
            Set<String> tempSet = Sets.newHashSet();
            for (String header : headersToSign) {
                tempSet.add(header.trim().toLowerCase());
            }
            headersToSign = tempSet;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if ((headersToSign != null && headersToSign.contains(key.toLowerCase())
                        && !Headers.AUTHORIZATION.equalsIgnoreCase(key))
                        || (headersToSign == null && isDefaultHeaderToSign(key))) {
                    ret.put(key, entry.getValue());
                }
            }
        }
        return ret;
    }

    private static boolean isDefaultHeaderToSign(String header) {
        header = header.trim().toLowerCase();
        return header.startsWith(Headers.YOP_PREFIX) || defaultHeadersToSign.contains(header);
    }

    /**
     * 初始化分块上传
     *
     * @param req 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse initMultipartUpload(InitiateMultipartUploadRequest req) throws IOException {
        CheckUtils.notNull(req,"req");
        CheckUtils.notEmpty(req.getApiUri(), "req.apiUri");
        CheckUtils.notEmpty(req.getBizCode(), "req.bizCode");
        YopRequest request = getYopRequest(req.getAppKey(), req.getSecretKey(), req.getNeedEncrypt(), req.getEncryptKey());
        if (StringUtils.isNotEmpty(req.getFileName())) {
            request.addParam(YopConstants.MULTIPART_FILE_NAME, req.getFileName());
        } else {
            request.addNullParam(YopConstants.MULTIPART_FILE_NAME);
        }
        request.addNullParam(YopConstants.MULTIPART_UPLOADS);
        request.addParam(YopConstants.MULTIPART_BIZ_CODE, req.getBizCode());
        return internalInitMultipartUpload(req.getApiUri(), request);
    }

    private static YopResponse internalInitMultipartUpload(String apiUri, YopRequest request) throws IOException {
        HttpMethodName method = HttpMethodName.POST;
        CheckUtils.checkApiUri(apiUri);
        if (BooleanUtils.isTrue(request.isNeedEncrypt())) {
            encryptRequest(request);
        }
        sign(apiUri, request, method);
        String contentUrl = richRequest(apiUri, request);
        // 将uploads，bizCode,fileName设置到query位置
        StringBuilder sb = new StringBuilder();
        sb.append(contentUrl)
                .append("?uploads=")
                .append("&bizCode=").append(request.getParamValue(YopConstants.MULTIPART_BIZ_CODE))
                .append("&fileName=").append(request.getParamValue(YopConstants.MULTIPART_FILE_NAME));
        request.getParams().clear();
        HttpUriRequest httpPost = buildFormHttpRequest(request, sb.toString(), method);
        YopResponse response = fetchContentByApacheHttpClient(httpPost, new ResponseConfig()
                .withNeedEncrypt(request.isNeedEncrypt())
                .withEncryptKey(request.getEncryptKey())
                .withYopPublicKey(InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048)));
        handleRsaResult(response);
        return response;
    }

    /**
     * 分块上传
     *
     * @param req 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse uploadPart(UploadPartRequest req) throws IOException {
        CheckUtils.notNull(req,"req");
        CheckUtils.notEmpty(req.getApiUri(), "req.apiUri");
        CheckUtils.notNull(req.getFile(), "req.file");
        CheckUtils.notEmpty(req.getUploadId(), "req.uploadId");
        CheckUtils.notEmpty(req.getBucket(), "req.bucket");
        CheckUtils.notEmpty(req.getKey(), "req.key");
        if (req.getPartSize() > YopConstants.FILE_MULTIPART_PART_SIZE) {
            throw new YopClientException("PartNumber " + req.getPartNumber() + " : Part Size should not be more than 5M.");
        }
        YopRequest request = getYopRequest(req.getAppKey(), req.getSecretKey(), req.getNeedEncrypt(), req.getEncryptKey());
        request.addParam(YopConstants.MULTIPART_PART_NUMBER, req.getPartNumber());
        request.addParam(YopConstants.MULTIPART_UPLOAD_ID, req.getUploadId());
        request.addParam(YopConstants.MULTIPART_BUCKET, req.getBucket());
        request.addParam(YopConstants.MULTIPART_KEY, req.getKey());
        return internalUploadPart(req.getApiUri(), request, req.getFile(), req.getPartSize());
    }

    private static YopResponse internalUploadPart(String apiUri, YopRequest request, Object file, int partSize) throws IOException {
        HttpMethodName method = HttpMethodName.PUT;
        CheckUtils.checkApiUri(apiUri);
        if (BooleanUtils.isTrue(request.isNeedEncrypt())) {
            encryptRequest(request);
        }
        sign(apiUri, request, method);
        String contentUrl = richRequest(apiUri, request);
        // 将partNumber，uploadId,bucket,key设置到query位置
        StringBuilder sb = new StringBuilder();
        sb.append(contentUrl)
                .append("?partNumber=").append(request.getParamValue(YopConstants.MULTIPART_PART_NUMBER))
                .append("&uploadId=").append(request.getParamValue(YopConstants.MULTIPART_UPLOAD_ID))
                .append("&bucket=").append(request.getParamValue(YopConstants.MULTIPART_BUCKET))
                .append("&key=").append(request.getParamValue(YopConstants.MULTIPART_KEY));
        // 移除partNumber，uploadId,bucket,key参数，避免后面被设置到body
        request.getParams().clear();
        Pair<HttpUriRequest, CheckedInputStream> pair = buildMultiPartUploadRequest(request, sb.toString(), file, partSize);
        YopResponse response = fetchContentByApacheHttpClient(pair.getLeft(), new ResponseConfig()
                .withNeedEncrypt(request.isNeedEncrypt())
                .withEncryptKey(request.getEncryptKey())
                .withYopPublicKey(InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048)));
        handleRsaResult(response);
        if (pair.getRight() != null) {
            checkFileIntegrity(response, CRC64Utils.getCRC64(pair.getRight()));
        }
        return response;
    }

    /**
     * 完成分块上传
     *
     * @param req 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse completeMultipartUpload(CompleteMultipartUploadRequest req) throws IOException {
        CheckUtils.notNull(req,"req");
        CheckUtils.notEmpty(req.getApiUri(), "req.apiUri");
        CheckUtils.notEmpty(req.getUploadId(), "req.uploadId");
        CheckUtils.notEmpty(req.getBucket(), "req.bucket");
        CheckUtils.notEmpty(req.getKey(), "req.key");
        YopRequest request = getYopRequest(req.getAppKey(), req.getSecretKey(), req.getNeedEncrypt(), req.getEncryptKey());
        String reqBody = JsonUtils.toJsonString(req);
        return internalCompleteMultipartUpload(req.getApiUri(), request, reqBody);
    }

    private static YopResponse internalCompleteMultipartUpload(String apiUri, YopRequest request, String jsonString) throws IOException {
        HttpMethodName method = HttpMethodName.POST;
        CheckUtils.checkApiUri(apiUri);
        if (BooleanUtils.isTrue(request.isNeedEncrypt())) {
            encryptRequest(request);
        }
        sign(apiUri, request, method);
        String contentUrl = richRequest(apiUri, request);
        HttpUriRequest httpPost = buildJsonHttpRequest(request, contentUrl, method, jsonString);
        YopResponse response = fetchContentByApacheHttpClient(httpPost, new ResponseConfig()
                .withNeedEncrypt(request.isNeedEncrypt())
                .withEncryptKey(request.getEncryptKey())
                .withYopPublicKey(InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048)));
        handleRsaResult(response);
        return response;
    }

    /**
     * 取消分块上传
     *
     * @param req 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse abortMultipartUpload(AbortMultipartUploadRequest req) throws IOException {
        CheckUtils.notNull(req,"req");
        CheckUtils.notEmpty(req.getApiUri(), "req.apiUri");
        CheckUtils.notEmpty(req.getUploadId(), "req.uploadId");
        CheckUtils.notEmpty(req.getBucket(), "req.bucket");
        CheckUtils.notEmpty(req.getKey(), "req.key");
        YopRequest request = getYopRequest(req.getAppKey(), req.getSecretKey(), req.getNeedEncrypt(), req.getEncryptKey());
        request.addParam(YopConstants.MULTIPART_UPLOAD_ID, req.getUploadId());
        request.addParam(YopConstants.MULTIPART_BUCKET, req.getBucket());
        request.addParam(YopConstants.MULTIPART_KEY, req.getKey());
        return internalAbortMultipartUpload(req.getApiUri(), request);
    }

    private static YopRequest getYopRequest(String appKey, String secretKey, Boolean needEncrypt, String encryptKey) {
        YopRequest request = null;
        if (StringUtils.isEmpty(appKey) && StringUtils.isEmpty(secretKey)) {
            request = new YopRequest();
        } else if (!StringUtils.isEmpty(appKey) && !StringUtils.isEmpty(secretKey)) {
            request = new YopRequest(appKey, secretKey);
        } else {
            request = new YopRequest(appKey);
        }
        request.setNeedEncrypt(needEncrypt);
        request.setEncryptKey(encryptKey);
        return request;
    }

    private static YopResponse internalAbortMultipartUpload(String apiUri, YopRequest request) throws IOException {
        HttpMethodName method = HttpMethodName.DELETE;
        CheckUtils.checkApiUri(apiUri);
        if (BooleanUtils.isTrue(request.isNeedEncrypt())) {
            encryptRequest(request);
        }
        sign(apiUri, request, method);
        String contentUrl = richRequest(apiUri, request);
        // 将uploadId，bucket,key设置到query位置
        StringBuilder sb = new StringBuilder();
        sb.append(contentUrl)
                .append("?uploadId=").append(request.getParamValue(YopConstants.MULTIPART_UPLOAD_ID))
                .append("&bucket=").append(request.getParamValue(YopConstants.MULTIPART_BUCKET))
                .append("&key=").append(request.getParamValue(YopConstants.MULTIPART_KEY));
        // 移除uploadId,bucket,key参数，避免后面被设置到body
        request.getParams().clear();
        HttpUriRequest httpPost = buildFormHttpRequest(request, sb.toString(), method);
        YopResponse response = fetchContentByApacheHttpClient(httpPost, new ResponseConfig()
                .withNeedEncrypt(request.isNeedEncrypt())
                .withEncryptKey(request.getEncryptKey())
                .withYopPublicKey(InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048)));
        handleRsaResult(response);
        return response;
    }

    /**
     * 文件上传
     *
     * @param req 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse multipartUpload(UploadFileRequest req) throws IOException {
        CheckUtils.notNull(req,"req");
        CheckUtils.notEmpty(req.getApiUri(), "req.apiUri");
        CheckUtils.notEmpty(req.getBizCode(), "req.bizCode");
        CheckUtils.notEmpty(req.getUploadFlag(), "req.uploadFlag");
        CheckUtils.notEmpty(req.getFile(), "req.file");
        YopRequest request = getYopRequest(req.getAppKey(), req.getSecretKey(), req.getNeedEncrypt(), req.getEncryptKey());
        request.addParam(YopConstants.MULTIPART_BIZ_CODE, req.getBizCode());
        // 商户直接请求小文件上传
        if (StringUtils.equals(YopConstants.MULTIPART_UPLOAD_FLAG_1, req.getUploadFlag())) {
            return callSmallFileUpload(request, req);
        } else {
            // 商户请求非小文件上传
            //filePropertiesPair left : true-读取之前可以拿到文件流大小 false-读取之前拿不到文件流大小 right : 文件流大小
            Pair<Boolean, Long> filePropertiesPair = getFilePropertiesPair(req.getFile());
            if (filePropertiesPair.getLeft()) {
                //根据文件大小判断走小文件上传还是分块上传
                return uploadFileByFileSize(request, req, filePropertiesPair.getRight());
            } else {
                //不考虑文件大小强制走分块上传
                request.addNullParam(YopConstants.MULTIPART_UPLOADS);
                return uploadPartFile(req.getApiUri(), request, req);
            }
        }
    }

    private static YopResponse internalUploadSmallFile(String apiUri, YopRequest request, Object file, int partSize) throws IOException {
        HttpMethodName method = HttpMethodName.PUT;
        CheckUtils.checkApiUri(apiUri);
        if (BooleanUtils.isTrue(request.isNeedEncrypt())) {
            encryptRequest(request);
        }
        sign(apiUri, request, method);
        String contentUrl = richRequest(apiUri, request);
        // 将bizCode设置到query位置
        StringBuilder sb = new StringBuilder();
        sb.append(contentUrl)
                .append("?bizCode=").append(request.getParamValue(YopConstants.MULTIPART_BIZ_CODE))
                .append("&fileName=").append(request.getParamValue(YopConstants.MULTIPART_FILE_NAME));
        // 移除bizCode参数，避免后面被设置到body
        //request.getParams().asMap().remove(YopConstants.MULTIPART_BIZ_CODE);
        Pair<HttpUriRequest, CheckedInputStream> pair = buildMultiPartUploadRequest(request, sb.toString(), file, partSize);
        YopResponse response = fetchContentByApacheHttpClient(pair.getLeft(), new ResponseConfig()
                .withNeedEncrypt(request.isNeedEncrypt())
                .withEncryptKey(request.getEncryptKey())
                .withYopPublicKey(InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048)));
        handleRsaResult(response);
        if (pair.getRight() != null) {
            checkFileIntegrity(response, CRC64Utils.getCRC64(pair.getRight()));
        }
        return response;
    }

    private static YopResponse uploadFileByFileSize(YopRequest request, UploadFileRequest req, long fileLength) throws IOException {
        if (fileLength <= YopConstants.FILE_MULTIPART_PART_SIZE) {
            setFileName(request, req.getFileName(), req.getFile());
            return internalUploadSmallFile(req.getApiUri(), request, req.getFile(), (int) fileLength);
        } else {
            if (fileLength > YopConstants.FILE_MULTIPART_UPLOAD_SIZE) {
                throw new YopClientException(" one file should not be more than 25M.");
            }
            request.addNullParam(YopConstants.MULTIPART_UPLOADS);
            return uploadPartFile(req.getApiUri(), request, req);
        }
    }

    private static YopResponse callSmallFileUpload(YopRequest request, UploadFileRequest req) throws IOException {
        InputStream is = null;
        ByteArrayInputStream bais = null;
        try {
            setFileName(request, req.getFileName(), req.getFile());
            Pair<Boolean, InputStream> pair = getInputStreamPair(req.getFile());
            is = pair.getRight();
            // 小文件上传理论上是不需要本地读取的，但不能保证商户上传的都是小文件，所以读取判断控制一下
            bais = new ByteArrayInputStream(IOUtil.toByteArray(is, YopConstants.FILE_MULTIPART_PART_SIZE));
            if (is.read() != -1) {
                throw new YopClientException(" one file should not be more than 5M.");
            }
            return internalUploadSmallFile(req.getApiUri(), request, bais, bais.available());
        } finally {
            closeStream(is, bais);
        }
    }

    private static void setFileName(YopRequest request, String fileName, Object file) throws IOException {
        if (StringUtils.isNotEmpty(fileName)) {
            request.addParam(YopConstants.MULTIPART_FILE_NAME, fileName);
        } else {
            String fileNm = getFileName(file);
            if (StringUtils.isNotEmpty(fileNm)) {
                request.addParam(YopConstants.MULTIPART_FILE_NAME, fileNm);
            } else {
                request.addNullParam(YopConstants.MULTIPART_FILE_NAME);
            }
        }
    }

    private static void closeStream(InputStream is, ByteArrayInputStream bais) {
        try {
            if (is != null) {
                is.close();
            }
            if (bais != null) {
                bais.close();
            }
        } catch (IOException ioEx) {
            LOGGER.error("upload file exception occurred when close stream", ioEx);
        }
    }

    private static YopResponse uploadPartFile(String apiUri, YopRequest request, UploadFileRequest req) throws IOException {
        InputStream is = null;
        ByteArrayInputStream bais = null;
        List<PartETag> partETags = new ArrayList<PartETag>();
        String uploadId;
        String bucket;
        String key;
        setFileName(request, req.getFileName(), req.getFile());
        //1、初始化分块,获取uploadId
        LOGGER.debug("multipart upload init,req : " + req.toString());
        YopResponse response = internalInitMultipartUpload(apiUri, request);
        LOGGER.debug("multipart upload init,res : " + response.toString());
        if (!response.isSuccess()) {
            return response;
        }
        Map resultMap = (Map) response.getResult();
        uploadId = String.valueOf(resultMap.get("uploadId"));
        bucket = String.valueOf(resultMap.get("bucket"));
        key = String.valueOf(resultMap.get("key"));
        //2、遍历分块上传
        try {
            long totalRead = 0L;
            for (int i = 0; ; i++) {
                YopRequest uploadYopRequest = getYopRequest(req.getAppKey(), req.getSecretKey(), req.getNeedEncrypt(), req.getEncryptKey());
                long startPos = i * YopConstants.FILE_MULTIPART_PART_SIZE;
                Pair<Boolean, InputStream> pair = getInputStreamPair(req.getFile());
                Boolean skipFlag = pair.getLeft();
                is = pair.getRight();
                if (skipFlag) {
                    //跳过已经上传的分片
                    is.skip(startPos);
                }
                int partSize = (int) YopConstants.FILE_MULTIPART_PART_SIZE;
                byte[] bytes = new byte[partSize];
                int offset;
                int readed = 0;
                for (offset = 0; offset < partSize && (readed = is.read(bytes, offset, partSize - offset)) != -1; offset += readed) {

                }
                if (offset <= 0 && readed == -1) {
                    break;
                }
                totalRead = totalRead + offset;
                if (totalRead > YopConstants.FILE_MULTIPART_UPLOAD_SIZE) {
                    throw new YopClientException("one file should not be more than 25M.");
                }
                bais = new ByteArrayInputStream(bytes, 0, offset);
                //设置分片号。每一个上传的分片都有一个分片号
                uploadYopRequest.addParam(YopConstants.MULTIPART_PART_NUMBER, i + 1);
                uploadYopRequest.addParam(YopConstants.MULTIPART_UPLOAD_ID, uploadId);
                uploadYopRequest.addParam(YopConstants.MULTIPART_BUCKET, bucket);
                uploadYopRequest.addParam(YopConstants.MULTIPART_KEY, key);
                LOGGER.debug("multipart upload part,partNumber : " + (i + 1) + " partSize : " + offset);
                YopResponse uploadYopResponse = internalUploadPart(apiUri, uploadYopRequest, bais, offset);
                LOGGER.debug("multipart upload part,res : " + uploadYopResponse.toString());
                if (!uploadYopResponse.isSuccess()) {
                    //分块上传某块失败后，调用取消接口，避免留存无用废片
                    return callAbortMultipartUpload(request, req, uploadId, bucket, key);
                }
                //每次上传分片之后，返回结果会包含一个PartETag。PartETag将被保存到partETags中。
                partETags.add(new PartETag(i + 1, uploadYopResponse.getHeaders().get(Headers.ETAG)));
            }
        } catch (Throwable ex) {
            LOGGER.error("multipart upload file error ", ex);
            return callAbortMultipartUpload(request, req, uploadId, bucket, key);
        } finally {
            closeStream(is, bais);
        }

        //3、完成分块上传
        YopRequest completeYopRequest = getYopRequest(req.getAppKey(), req.getSecretKey(), req.getNeedEncrypt(), req.getEncryptKey());
        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest();
        completeMultipartUploadRequest.setUploadId(uploadId);
        completeMultipartUploadRequest.setBucket(bucket);
        completeMultipartUploadRequest.setKey(key);
        completeMultipartUploadRequest.setParts(partETags);
        String reqBody = JsonUtils.toJsonString(completeMultipartUploadRequest);
        LOGGER.debug("multipart upload complete,req : " + reqBody);
        YopResponse completeYopResponse = internalCompleteMultipartUpload(apiUri, completeYopRequest, reqBody);
        LOGGER.debug("multipart upload complete,res : " + completeYopResponse.toString());
        return completeYopResponse;
    }

    private static YopResponse callAbortMultipartUpload(YopRequest request, UploadFileRequest req, String uploadId, String bucket, String key) throws IOException {
        YopRequest abortYopRequest = getYopRequest(req.getAppKey(), req.getSecretKey(), req.getNeedEncrypt(), req.getEncryptKey());
        abortYopRequest.addParam(YopConstants.MULTIPART_UPLOAD_ID, uploadId);
        abortYopRequest.addParam(YopConstants.MULTIPART_BUCKET, bucket);
        abortYopRequest.addParam(YopConstants.MULTIPART_KEY, key);
        YopResponse abortYopResponse = internalAbortMultipartUpload(req.getApiUri(), abortYopRequest);
        if (abortYopResponse.isSuccess()) {//取消成功，返回上传文件失败
            abortYopResponse.setState("FAILURE");
            abortYopResponse.setError(getFileUploadError());
            return abortYopResponse;
        }
        return abortYopResponse;//取消失败，直接返回错误信息
    }

    /**
     * 获取FilePropertiesPair
     *
     * @param file
     * @return Pair<Boolean, Long> left : true-读取之前可以拿到文件流大小 false-读取之前拿不到文件流大小 right : 文件流大小
     * @throws IOException io异常
     */
    private static Pair<Boolean, Long> getFilePropertiesPair(Object file) throws IOException {
        long fileLength = 0L;
        if (file instanceof String) {
            fileLength = new FileInputStream(new File((String) file)).available();
            return new ImmutablePair<Boolean, Long>(true, fileLength);
        } else if (file instanceof File) {
            fileLength = new FileInputStream((File) file).available();
            return new ImmutablePair<Boolean, Long>(true, fileLength);
        } else if (file instanceof FileInputStream) {
            fileLength = ((FileInputStream) file).available();
            return new ImmutablePair<Boolean, Long>(true, fileLength);
        } else if (file instanceof InputStream) {
            return new ImmutablePair<Boolean, Long>(false, fileLength);
        } else {
            throw new YopClientException("不支持的上传文件类型");
        }
    }

    /**
     * 获取InputStreamPair
     *
     * @param file
     * @return Pair<Boolean, InputStream> true-读取流时需要按指定位置skip false-读取流时不需要skip
     * @throws IOException io异常
     */
    public static Pair<Boolean, InputStream> getInputStreamPair(Object file) throws FileNotFoundException {
        if (file instanceof String) {
            return new ImmutablePair<Boolean, InputStream>(true, new FileInputStream(new File((String) file)));
        } else if (file instanceof File) {
            return new ImmutablePair<Boolean, InputStream>(true, new FileInputStream((File) file));
        } else if (file instanceof FileInputStream) {
            return new ImmutablePair<Boolean, InputStream>(false, (FileInputStream) file);
        } else if (file instanceof InputStream) {
            return new ImmutablePair<Boolean, InputStream>(false, (InputStream) file);
        }else {
            throw new YopClientException("不支持的上传文件类型");
        }
    }

    public static String getFileName(Object file) throws FileNotFoundException, IOException {
        if (file instanceof String) {
            return new File((String) file).getName();
        } else if (file instanceof File) {
            return ((File) file).getName();
        } else if (file instanceof FileInputStream) {
            MarkableFileInputStream in = new MarkableFileInputStream((FileInputStream) file);
            in.mark(0);
            //解析文件扩展名的时候会读取流的前64*1024个字节,需要reset文件流
            String fileName = FileUtils.getFileName(in);
            in.reset();
            return fileName;
        } else if (file instanceof InputStream) {
            return "";
        }else {
            throw new YopClientException("不支持的上传文件类型");
        }
    }
}
