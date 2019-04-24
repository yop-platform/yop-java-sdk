package com.yeepay.g3.sdk.yop.client;

import com.google.common.collect.Maps;
import com.yeepay.g3.sdk.yop.client.router.GateWayRouter;
import com.yeepay.g3.sdk.yop.client.router.ServerRootSpace;
import com.yeepay.g3.sdk.yop.client.router.SimpleGateWayRouter;
import com.yeepay.g3.sdk.yop.config.AppSdkConfig;
import com.yeepay.g3.sdk.yop.config.AppSdkConfigProvider;
import com.yeepay.g3.sdk.yop.config.AppSdkConfigProviderRegistry;
import com.yeepay.g3.sdk.yop.config.support.BackUpAppSdkConfigManager;
import com.yeepay.g3.sdk.yop.encrypt.AESEncrypter;
import com.yeepay.g3.sdk.yop.encrypt.DigestAlgEnum;
import com.yeepay.g3.sdk.yop.encrypt.RSA;
import com.yeepay.g3.sdk.yop.error.YopError;
import com.yeepay.g3.sdk.yop.exception.VerifySignFailedException;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.http.HttpMethodName;
import com.yeepay.g3.sdk.yop.http.YopHttpResponse;
import com.yeepay.g3.sdk.yop.model.YopErrorResponse;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.CharacterConstants;
import com.yeepay.g3.sdk.yop.utils.FileUtils;
import com.yeepay.g3.sdk.yop.utils.InternalConfig;
import com.yeepay.g3.sdk.yop.utils.checksum.CRC64;
import com.yeepay.g3.sdk.yop.utils.io.MarkableFileInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.CheckedInputStream;

import static com.yeepay.g3.sdk.yop.utils.CharacterConstants.EMPTY;

public class AbstractClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClient.class);

    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final int EXT_READ_BUFFER_SIZE = 64 * 1024;

    private static final GateWayRouter GATE_WAY_ROUTER;

    private static CloseableHttpClient httpClient;

    private static org.apache.http.client.config.RequestConfig.Builder requestConfigBuilder;
    private static CredentialsProvider credentialsProvider;
    private static HttpHost proxyHttpHost;

    protected static final String SESSION_ID = getUUID();

    private static final YopError FILE_CHECK_ERROR;

    static {
        initApacheHttpClient();
        FILE_CHECK_ERROR = new YopError();
        FILE_CHECK_ERROR.setCode("40044");
        FILE_CHECK_ERROR.setMessage("业务处理失败");
        FILE_CHECK_ERROR.setSubCode("isv.scene.filestore.put.crc-failed");
        FILE_CHECK_ERROR.setSubMessage("文件上传crc校验失败");

        AppSdkConfigProvider sdkConfigProvider = AppSdkConfigProviderRegistry.getProvider();
        AppSdkConfig appSdkConfig = sdkConfigProvider.getDefaultConfig() == null ? BackUpAppSdkConfigManager.getBackUpConfig()
                : sdkConfigProvider.getDefaultConfig();
        ServerRootSpace serverRootSpace;
        try {
            serverRootSpace = new ServerRootSpace(
                    StringUtils.defaultIfBlank(appSdkConfig.getServerRoot(), YopConstants.DEFAULT_SERVER_ROOT),
                    StringUtils.defaultIfBlank(appSdkConfig.getYosServerRoot(), YopConstants.DEFAULT_YOS_SERVER_ROOT),
                    StringUtils.defaultIfBlank(appSdkConfig.getSandboxServerRoot(), YopConstants.DEFAULT_SANDBOX_SERVER_ROOT));
        } catch (MalformedURLException e) {
            throw new YopClientException("server root illegal");
        }
        GATE_WAY_ROUTER = new SimpleGateWayRouter(serverRootSpace);
    }

    // 创建包含connection pool与超时设置的client
    public static void initApacheHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(InternalConfig.READ_TIMEOUT)
                .setConnectTimeout(InternalConfig.CONNECT_TIMEOUT)
                .build();

        httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(InternalConfig.MAX_CONN_TOTAL)
                .setMaxConnPerRoute(InternalConfig.MAX_CONN_PER_ROUTE)
                .setSSLSocketFactory(InternalConfig.TRUST_ALL_CERTS ? getTrustedAllSSLConnectionSocketFactory() : null)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(30, TimeUnit.SECONDS)
                .setRetryHandler(new YopHttpRequestRetryHandler())
                .setKeepAliveStrategy(new YopConnectionKeepAliveStrategy())
                .build();

        requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectTimeout(InternalConfig.CONNECT_TIMEOUT);
        requestConfigBuilder.setStaleConnectionCheckEnabled(true);
        /*if (InternalConfig.getLocalAddress() != null) {
            requestConfigBuilder.setLocalAddress(config.getLocalAddress());
        }*/

        if (InternalConfig.proxy != null) {
            String proxyHost = InternalConfig.proxy.getHost();
            int proxyPort = InternalConfig.proxy.getPort();
            String scheme = InternalConfig.proxy.getScheme();
            if (proxyHost != null && proxyPort > 0) {
                proxyHttpHost = new HttpHost(proxyHost, proxyPort, scheme);
                requestConfigBuilder.setProxy(proxyHttpHost);
                credentialsProvider = new BasicCredentialsProvider();
                String proxyUsername = InternalConfig.proxy.getUsername();
                String proxyPassword = InternalConfig.proxy.getPassword();
                String proxyDomain = InternalConfig.proxy.getDomain();
                String proxyWorkstation = InternalConfig.proxy.getWorkstation();
                if (proxyUsername != null && proxyPassword != null) {
                    credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
                            new NTCredentials(proxyUsername, proxyPassword,
                                    proxyWorkstation, proxyDomain));
                }
            }
        }
    }

    public static void destroyApacheHttpClient() {
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.error("httpclient close fail", e);
        }
    }

    private static SSLConnectionSocketFactory getTrustedAllSSLConnectionSocketFactory() {
        LOGGER.warn("[yop-sdk]已设置信任所有证书。仅供内测使用，请勿在生产环境配置。");
        SSLConnectionSocketFactory sslConnectionSocketFactory = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build());
        } catch (Exception e) {
            LOGGER.error("error when get trust-all-certs request factory,will return normal request factory instead", e);
        }
        return sslConnectionSocketFactory;
    }

    /**
     * 构建普通formHttp请求
     *
     * @param request    yop请求
     * @param contentUrl 请求地址
     * @param httpMethod http方法
     * @return http请求
     * @throws IOException io异常
     */
    protected static HttpUriRequest buildFormHttpRequest(YopRequest request, String contentUrl, HttpMethodName httpMethod) {
        RequestBuilder requestBuilder;
        if (HttpMethodName.POST == httpMethod) {
            requestBuilder = RequestBuilder.post();
        } else if (HttpMethodName.GET == httpMethod) {
            requestBuilder = RequestBuilder.get();
        } else {
            throw new YopClientException("unsupported http method");
        }
        requestBuilder.setUri(contentUrl);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        try {
            for (Map.Entry<String, Collection<String>> entry : request.getParams().asMap().entrySet()) {
                String paramKey = entry.getKey();
                for (String value : entry.getValue()) {
                    requestBuilder.addParameter(paramKey, URLEncoder.encode(value, YopConstants.ENCODING));
                }
            }
        } catch (IOException ex) {
            throw new YopClientException("unable to create http request.", ex);
        }
        return requestBuilder.build();
    }

    /**
     * 构建multiFormRequest
     *
     * @param request    yop请求
     * @param contentUrl 请求地址
     * @return key为http请求，value为checkInputStream列表
     * @throws IOException io异常
     */
    protected static Pair<HttpUriRequest, List<CheckedInputStream>> buildMultiFormRequest(YopRequest request, String contentUrl) {
        RequestBuilder requestBuilder = RequestBuilder.post().setUri(contentUrl);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        TreeMap<String, CheckedInputStream> checkedInputStreams = null;
        try {
            if (!request.hasFiles()) {
                for (Map.Entry<String, Collection<String>> entry : request.getParams().asMap().entrySet()) {
                    String paramKey = entry.getKey();
                    for (String value : entry.getValue()) {
                        requestBuilder.addParameter(paramKey, URLEncoder.encode(value, YopConstants.ENCODING));
                    }
                }
            } else {
                checkedInputStreams = Maps.newTreeMap();
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setCharset(Charset.forName(YopConstants.ENCODING));
                for (Map.Entry<String, Object> entry : request.getMultipartFiles().entrySet()) {
                    String paramName = entry.getKey();
                    Pair<String, CheckedInputStream> checkedInputStreamPair = wrapToCheckInputStream(entry.getValue());
                    multipartEntityBuilder.addBinaryBody(paramName, checkedInputStreamPair.getRight(), ContentType.DEFAULT_BINARY, checkedInputStreamPair.getLeft());
                    checkedInputStreams.put(paramName, checkedInputStreamPair.getRight());
                }
                for (Map.Entry<String, String> entry : request.getParams().entries()) {
                    multipartEntityBuilder.addTextBody(entry.getKey(), URLEncoder.encode(entry.getValue(), YopConstants.ENCODING));
                }
                requestBuilder.setEntity(multipartEntityBuilder.build());
            }
        } catch (IOException ex) {
            throw new YopClientException("unable to create http request.", ex);
        }
        HttpUriRequest httpPost = requestBuilder.build();
        List<CheckedInputStream> inputStreamList = checkedInputStreams == null ? null : new ArrayList<CheckedInputStream>(checkedInputStreams.values());
        return new ImmutablePair<HttpUriRequest, List<CheckedInputStream>>(httpPost, inputStreamList);
    }

    /**
     * 包装为checkInputStream
     *
     * @param file 文件或者流
     * @return key为文件名称，value为流
     * @throws IOException io异常
     */
    private static Pair<String, CheckedInputStream> wrapToCheckInputStream(Object file) throws IOException {
        if (file instanceof String) {
            File paramFile = new File((String) file);
            CheckedInputStream inputStream = new CheckedInputStream(new FileInputStream(paramFile), new CRC64());
            return new ImmutablePair<String, CheckedInputStream>(paramFile.getName(), inputStream);
        }
        if (file instanceof File) {
            CheckedInputStream inputStream = new CheckedInputStream(new FileInputStream((File) file), new CRC64());
            return new ImmutablePair<String, CheckedInputStream>(((File) file).getName(), inputStream);
        }
        if (file instanceof FileInputStream) {
            return getCheckedInputStreamPair((FileInputStream) file);
        }
        if (file instanceof InputStream) {
            return getCheckedInputStreamPair((InputStream) file);
        }
        throw new YopClientException("不支持的上传文件类型");
    }

    private static Pair<String, CheckedInputStream> getCheckedInputStreamPair(FileInputStream fileInputStream) throws IOException {
        MarkableFileInputStream in = new MarkableFileInputStream(fileInputStream);
        in.mark(0);
        //解析文件扩展名的时候会读取流的前64*1024个字节,需要reset文件流
        String fileName = FileUtils.getFileName(in);
        in.reset();
        CheckedInputStream inputStream = new CheckedInputStream(in, new CRC64());
        return new ImmutablePair<String, CheckedInputStream>(fileName, inputStream);
    }

    private static Pair<String, CheckedInputStream> getCheckedInputStreamPair(InputStream inputStream) throws IOException {
        //解析文件扩展名的时候会读取流的前64*1024个字节
        byte[] extReadBuffer = new byte[EXT_READ_BUFFER_SIZE];
        int totalRead = 0;
        int lastRead = inputStream.read(extReadBuffer);
        while (lastRead != -1) {
            totalRead += lastRead;
            if (totalRead == EXT_READ_BUFFER_SIZE) {
                break;
            }
            lastRead = inputStream.read(extReadBuffer, totalRead, EXT_READ_BUFFER_SIZE - totalRead);
        }
        ByteArrayInputStream extReadIn = new ByteArrayInputStream(extReadBuffer, 0, totalRead);
        String fileName = FileUtils.getFileName(extReadIn);
        extReadIn.reset();
        SequenceInputStream sequenceInputStream = new SequenceInputStream(extReadIn, inputStream);
        return new ImmutablePair<String, CheckedInputStream>(fileName, new CheckedInputStream(sequenceInputStream,
                new CRC64()));
    }

    protected static YopResponse fetchContentByApacheHttpClient(HttpUriRequest request) throws IOException {
        return fetchContentByApacheHttpClient(request, ResponseConfig.NONE_OPERATION_CONFIG);
    }


    protected static YopResponse fetchContentByApacheHttpClient(HttpUriRequest request, ResponseConfig responseConfig) throws IOException {
        HttpContext httpContext = createHttpContext();
        CloseableHttpResponse remoteResponse = null;
        try {
            remoteResponse = getHttpClient().execute(request, httpContext);
            return parseResponse(remoteResponse, responseConfig);
        } catch (Throwable ex) {
            String requestId = getRequestId(request);
            LOGGER.error("request failure, requestId:" + requestId, ex);

            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else if (ex instanceof YopClientException) {
                throw (YopClientException) ex;
            } else {
                throw new YopClientException("unable to execute request.", ex);
            }
        } finally {
            HttpClientUtils.closeQuietly(remoteResponse);
        }
    }

    private static String getRequestId(HttpUriRequest request) {
        return request.getFirstHeader(Headers.YOP_REQUEST_ID).getValue();
    }

    protected static YopResponse parseResponse(CloseableHttpResponse response, ResponseConfig responseConfig) throws IOException {
        YopHttpResponse httpResponse = new YopHttpResponse(response);
        Header yopViaHeader = response.getFirstHeader(Headers.YOP_VIA);
        if (yopViaHeader != null && StringUtils.equals(yopViaHeader.getValue(), YopConstants.SANDBOX_GATEWAY_VIA)) {
            LOGGER.info("response from sandbox-gateway");
        }
        int statusCode = httpResponse.getStatusCode();
        if (statusCode / 100 == HttpStatus.SC_OK / 100 && statusCode != HttpStatus.SC_NO_CONTENT) {
            //not a error
            YopResponse yopResponse = new YopResponse();
            handleHeaders(yopResponse, response);
            yopResponse.setState("SUCCESS");
            yopResponse.setRequestId(httpResponse.getHeader(Headers.YOP_REQUEST_ID));
            if (httpResponse.getContent() != null) {
                if (isJsonResponse(response)) {
                    String content = IOUtils.toString(httpResponse.getContent(), YopConstants.ENCODING);
                    if (responseConfig != ResponseConfig.NONE_OPERATION_CONFIG) {
                        verifySignature(content, httpResponse.getHeader(Headers.YOP_SIGN), responseConfig.getYopPublicKey());
                        content = decryptResponse(content, responseConfig);
                    }
                    JacksonJsonMarshaller.load(content, yopResponse);
                    if (yopResponse.getStringResult() != null) {
                        yopResponse.setResult(JacksonJsonMarshaller.unmarshal(yopResponse.getStringResult(), Object.class));
                    }
                } else {
                    yopResponse.setResult(response.getEntity().getContent());
                }
            }
            return yopResponse;
        } else if (statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR && statusCode != HttpStatus.SC_BAD_GATEWAY) {
            if (httpResponse.getContent() != null) {
                String content = IOUtils.toString(httpResponse.getContent(), YopConstants.ENCODING);
                if (responseConfig != ResponseConfig.NONE_OPERATION_CONFIG) {
                    verifySignature(content, httpResponse.getHeader(Headers.YOP_SIGN), responseConfig.getYopPublicKey());
                    content = decryptResponse(content, responseConfig);
                }
                YopResponse yopResponse = new YopResponse();
                handleHeaders(yopResponse, response);
                yopResponse.setState("FAILURE");
                YopErrorResponse errorResponse = JacksonJsonMarshaller.unmarshal(content, YopErrorResponse.class);
                yopResponse.setRequestId(errorResponse.getRequestId());
                yopResponse.setError(YopError.Builder.anYopError()
                        .withCode(errorResponse.getCode())
                        .withSubCode(errorResponse.getSubCode())
                        .withMessage(errorResponse.getMessage())
                        .withSubMessage(errorResponse.getSubMessage())
                        .build());
                return yopResponse;
            } else {
                throw new YopClientException("empty result with httpStatusCode:" + httpResponse.getStatusCode());
            }
        }
        throw new YopClientException("unexpected httpStatusCode:" + httpResponse.getStatusCode());
    }

    private static String decryptResponse(String content, ResponseConfig response) {
        //只有需要解密而且网关确实返回密文的情况下才解密（某些情况下即使请求加密，网关也无法正常对结果加密）
        if (response.isNeedDecrypt() && !StringUtils.startsWith(content, CharacterConstants.LEFT_BRACE)) {
            return AESEncrypter.decrypt(content, response.getDecryptKey());
        }
        return content;
    }

    private static void verifySignature(String content, String signature, PublicKey yopPublicKey) {
        if (StringUtils.isEmpty(signature)) {
            return;
        }
        //本版本sdk摘要算法只有sha256
        content = content.replaceAll("[ \t\n]", EMPTY);
        if (!RSA.verifySign(content, signature, yopPublicKey, DigestAlgEnum.SHA256)) {
            throw new VerifySignFailedException("response sign verify failure");
        }
    }

    private static boolean isJsonResponse(CloseableHttpResponse response) {
        return StringUtils.startsWith(response.getEntity().getContentType().getValue(), CONTENT_TYPE_JSON);
    }

    /**
     * 填充header
     *
     * @param yopResponse 业务response
     * @param response    httpResponse
     */
    private static void handleHeaders(YopResponse yopResponse, CloseableHttpResponse response) {
        HeaderIterator headerIterator = response.headerIterator();
        while (headerIterator.hasNext()) {
            Header header = headerIterator.nextHeader();
            if (StringUtils.startsWith(header.getName(), Headers.YOP_PREFIX)) {
                yopResponse.addHeader(header.getName(), header.getValue());
            }
        }
    }

    protected static void checkFileIntegrity(YopResponse response, String crc64) {
        if (response.isSuccess()) {
            String responseCrc64 = response.getHeaders().get(Headers.YOP_HASH_CRC64ECMA);
            if (null == responseCrc64 || StringUtils.equals(responseCrc64, crc64)) {
                return;
            }
            response.setState("FAILURE");
            response.setError(getFileCheckError());
        }
    }

    public static YopError getFileCheckError() {
        return FILE_CHECK_ERROR;
    }

    /**
     * Creates HttpClient Context object based on the internal request.
     *
     * @return HttpClient Context object.
     */
    private static HttpClientContext createHttpContext() {
        HttpClientContext context = HttpClientContext.create();
        context.setRequestConfig(requestConfigBuilder.build());
        if (credentialsProvider != null) {
            context.setCredentialsProvider(credentialsProvider);
        }
        /*if (config.isProxyPreemptiveAuthenticationEnabled()) {
            AuthCache authCache = new BasicAuthCache();
            authCache.put(proxyHttpHost, new BasicScheme());
            context.setAuthCache(authCache);
        }*/
        return context;
    }

    public static CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    protected static String richRequest(String methodOrUri, YopRequest request) {
        return GATE_WAY_ROUTER.route(methodOrUri, request) + methodOrUri;
    }

    protected static String getUUID() {
        return UUID.randomUUID().toString();
    }

}
