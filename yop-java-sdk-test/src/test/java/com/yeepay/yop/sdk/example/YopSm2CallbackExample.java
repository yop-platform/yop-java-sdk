/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.example;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.utils.Encodes;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;


/**
 * title: Sm2回调处理<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/10/23
 */
public class YopSm2CallbackExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSm2CallbackExample.class);

    //<appKey, 商户sm2私钥>
    private static Map<String, BCECPrivateKey> isvPrivateKeyMap;

    //<platformSerialNo,平台sm2公钥>
    private static Map<String, BCECPublicKey> yopPublicKeyMap;

    // 初始化国密算法类库，准备好商户密钥、平台公钥
    static {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            LOGGER.debug("BouncyCastleProvider added");
        } catch (Exception e) {
            LOGGER.warn("error when add BouncyCastleProvider", e);
        }
        isvPrivateKeyMap = new HashMap<>();
        isvPrivateKeyMap.put("app_15958159879157110009", string2PrivateKey("MIICBQIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBDzCCAQsCAQEEICvBlu1mNV6jIA8FdkKlRSga9cwXa0m+IBx9ERwtO1ZcoIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQE="));

        yopPublicKeyMap = new HashMap<>();
        // 此处key为16进制转换后的值，对应10进制的275568425014
        yopPublicKeyMap.put("4029287836", string2PublicKey("MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEd8OsYO4yIFNboF68nk1Yl9zquW/OuJSjGLz8Yu7ldV3ro9pGb5g079hWGeEZ+DqaHex3YzP7dVuQ9KV81pRa3w=="));

    }

    // mode 指定密文结构，旧标准的为C1C2C3，新的[《SM2密码算法使用规范》 GM/T 0009-2012]标准为C1C3C2
    // 我们采用C1C3C2
    // 根据mode不同，输出的密文C1C2C3排列顺序不同。C1为65字节第1字节为压缩标识，这里固定为0x04，后面64字节为xy分量各32字节。C3为32字节。C2长度与原文一致。
    private static final ThreadLocal<SM2Engine> engineThreadLocal = new ThreadLocal<SM2Engine>() {
        @Override
        protected SM2Engine initialValue() {
            return new SM2Engine(SM2Engine.Mode.C1C3C2);
        }
    };

    private static final String UTF_8 = "UTF-8";
    private static final String LF = "\n";
    private static final String SPACE = " ";
    private static final String DASH_LINE = "-";
    private static final String SLASH = "/";
    private static final String SEMICOLON = ";";
    private static final String COLON = ":";
    private static final String ENCRYPT_ALG = "SM4/CBC/PKCS5Padding";

    // TODO 解析易宝发起的http请求(post json格式)，会得到如下请求头、请求体(模拟测试数据)

    // 请求地址: 商户提供的回调地址
    private static String reqUri = "/xxx";
    // 请求头
    private static Map<String, String> reqHeaders;
    // 请求体
    private static String reqBody = "EZgjreIx_ZW-gIM2NtHoKSk2sMQ35eolEjZ76XPcCtEqbXRfv77Z2eUJHhfoN4TcAZjPykzzDJ2pH7FC8xbhXw";

    static {
        reqHeaders = Maps.newHashMap();
        reqHeaders.put("Authorization", "YOP-SM2-SM3 yop-auth-v3/app_15958159879157110009/2022-05-17T02:33:24Z/1800/content-length;content-type;x-yop-appkey;x-yop-content-sm3;x-yop-encrypt;x-yop-request-id/fuKri2WjLqmr_gKInxstDLn6zz9XPR518TKK2iF9sMROSEcWllrAxApO4ldPrjNPPc0UsAbitCxnumA3-CJt8A$SM3");
        reqHeaders.put("x-yop-content-sm3", "eaa5391d992058fce198590bcfb7f7a4533d8ea311ac97c964513d7da080351f");
        reqHeaders.put("x-yop-encrypt", "yop-encrypt-v1/275568425014/SM4_CBC_PKCS5Padding/BEmuYglu6Y0M5jkqZN_yssw137rWIiaB0ToXJXsQytFDSwau5sMGnPKCnEe-2Bgg_ThowDqOdcGnsvzATS4ol4rk_fSPebBPMvnjyWZk5hpMYPJxCCEJ80MgHYE3pBt50LulUCaCYhYDyf4VO5rYyjQ/u3E2PbLDjeiZi9IeQm7xyA/stream//JA");
        reqHeaders.put("x-yop-request-id", "wuTest1652754804319");
        reqHeaders.put("x-yop-sign-serial-no", "275568425014");
        reqHeaders.put("x-yop-appkey", "app_15958159879157110009");
        reqHeaders.put("Content-Type", "application/json");
    }

    public static void main(String[] args) throws Exception {
        // TODO 从易宝回调请求中解析
        final Map<String, String> headers = reqHeaders;
        Map<String, String> canonicalHeaders = new HashMap<>();
        headers.forEach((k,v) -> canonicalHeaders.put(k.trim().toLowerCase(), v));

        // 解析认证头
        String authorization = canonicalHeaders.get("authorization");
        String[] protocol = authorization.split(SPACE);
        String[] authorizationHeaders = StringUtils.split(protocol[1], SLASH);

        String signature = authorizationHeaders[5].split("\\$")[0];
        String platformSerialNo = canonicalHeaders.get("x-yop-sign-serial-no");
        if (StringUtils.isBlank(platformSerialNo)) {
            platformSerialNo = canonicalHeaders.get("x-yop-serial-no");
        }
        platformSerialNo = parseToHex(platformSerialNo);

        // 构造待认证字符串
        String canonicalReqString = buildCanonicalReqString(reqUri, canonicalHeaders);

        // 验证签名
        verifySign(canonicalReqString, signature, yopPublicKeyMap.get(platformSerialNo));

        // TODO 从易宝请求中解析
        String jsonReqBody = reqBody;
        String yopEncrypt = canonicalHeaders.get("x-yop-encrypt");
        // 解析加密头
        String[] items = StringUtils.splitPreserveAllTokens(yopEncrypt, SLASH);
        String iv = null;
        if (StringUtils.isNotBlank(items[4])) {
            String[] iv_AAD = StringUtils.splitPreserveAllTokens(items[4], SEMICOLON);
            iv = iv_AAD[0];
        }
        String appKey = canonicalHeaders.get("x-yop-appkey");
        String encryptedCredentialStr = items[3];
        assert StringUtils.isNoneBlank(appKey, encryptedCredentialStr);
        // 解密会话密钥
        byte[] encryptedCredentialBytes = decodeBase64(encryptedCredentialStr);
        byte[] decryptedSecretKey = decryptKey(encryptedCredentialBytes, isvPrivateKeyMap.get(appKey));
        assert null != decryptedSecretKey;

        // 解密业务参数
        final String bizContent = decryptBizContent(jsonReqBody, decryptedSecretKey, iv);
        LOGGER.info(bizContent);
        assert "{\"appId\":\"app_1595815987915711\",\"alias\":\"alias_0329\"}".equals(bizContent);
    }

    private static byte[] decryptKey(byte[] encryptedCredentialBytes, BCECPrivateKey isvPrivateKey) {
        try {
            SM2Engine engine = engineThreadLocal.get();
            ECPrivateKeyParameters priKeyParameters = convertPrivateKeyToParameters(isvPrivateKey);
            engine.init(false, priKeyParameters);
            return engine.processBlock(encryptedCredentialBytes, 0, encryptedCredentialBytes.length);
        } catch (Exception e) {
            throw new RuntimeException("error when decrypt work credential, ", e);
        }
    }

    private static String decryptBizContent(String jsonReqBody, byte[] decryptedSecretKey, String iv) {
        try {
            final Cipher cipher = Cipher.getInstance(ENCRYPT_ALG, BouncyCastleProvider.PROVIDER_NAME);
            Key sm4Key = new SecretKeySpec(decryptedSecretKey, "SM4");
            if (StringUtils.isNotBlank(iv)) {
                byte[] ivBytes = Encodes.decodeBase64(iv);
                IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
                cipher.init(Cipher.DECRYPT_MODE, sm4Key, ivParameterSpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, sm4Key);
            }
            return new String(cipher.doFinal(decodeBase64(jsonReqBody)), UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("error when decrypt bizContent, ", e);
        }
    }

    private static ECPrivateKeyParameters convertPrivateKeyToParameters(BCECPrivateKey ecPriKey) {
        ECParameterSpec parameterSpec = ecPriKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                parameterSpec.getN(), parameterSpec.getH());
        return new ECPrivateKeyParameters(ecPriKey.getD(), domainParameters);
    }

    private static String buildCanonicalReqString(String httpPath, Map<String, String> canonicalHeaders) {
        String authorization = canonicalHeaders.get("authorization");
        String[] protocol = authorization.split(SPACE);
        String protocolPrefix = protocol[0], protocolContent = protocol[1];
        String[] parts = protocolPrefix.split(DASH_LINE);
        String certType = parts[1];
        String digestAlg = parts[2];
        assert certType.equals("SM2") && digestAlg.equals("SM3");
        String[] authorizationHeaders = StringUtils.split(protocolContent, SLASH);
        String protocolVersion = authorizationHeaders[0];
        assert protocolVersion.equals("yop-auth-v3");
        String appKey = authorizationHeaders[1];
        String timestamp = authorizationHeaders[2];
        long expirationInSeconds = Long.parseLong(authorizationHeaders[3]);
        String signedHeaders = authorizationHeaders[4].toLowerCase();

        //authString
        String authString = new StringBuilder(protocolVersion).append(SLASH)
                .append(appKey).append(SLASH)
                .append(timestamp).append(SLASH)
                .append(expirationInSeconds).toString();

        // Formatting the URL with signing protocol.
        String canonicalURI = getCanonicalURIPath(httpPath);

        // Formatting the query string with signing protocol.
        String canonicalQueryString = getCanonicalQueryString();

        // Sorted the headers should be signed from the request.
        // Formatting the headers from the request based on signing protocol.
        String canonicalHeader = getCanonicalHeaders(signedHeaders, canonicalHeaders);
        return new StringBuilder(authString).append(LF)
                .append(httpPath).append(LF)
                .append(canonicalURI).append(LF)
                .append(canonicalQueryString).append(LF)
                .append(canonicalHeader).toString();
    }

    private static void verifySign(String canonicalReqString, String signature, BCECPublicKey publicKey) {
        try {
            byte[] signData = decodeBase64(signature);
            byte[] srcData = canonicalReqString.getBytes(UTF_8);
            ECParameterSpec parameterSpec = publicKey.getParameters();
            ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                    parameterSpec.getN(), parameterSpec.getH());
            ECPublicKeyParameters pubKeyParameters = new ECPublicKeyParameters(publicKey.getQ(), domainParameters);
            verify(pubKeyParameters, null, srcData, signData);
        } catch (IOException e) {
            throw new RuntimeException("UnexpectedError, VerifySign Fail, data:" +
                    canonicalReqString + ", sign:" + signature + ", key:" + publicKey + ", ex:", e);
        }

    }

    /**
     * 验签
     *
     * @param pubKeyParameters 公钥
     * @param withId           可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @param srcData          原文
     * @param sign             DER编码的签名值
     * @return 验签成功返回true，失败返回false
     */
    public static boolean verify(ECPublicKeyParameters pubKeyParameters, byte[] withId, byte[] srcData, byte[] sign) {
        SM2Signer signer = new SM2Signer();
        CipherParameters param;
        if (withId != null) {
            param = new ParametersWithID(pubKeyParameters, withId);
        } else {
            param = pubKeyParameters;
        }
        signer.init(false, param);
        signer.update(srcData, 0, srcData.length);
        return signer.verifySignature(sign);
    }

    private static String getCanonicalURIPath(String path) {
        if (path == null) {
            return "/";
        } else {
            return path.startsWith("/") ? normalizePath(path) : "/" + normalizePath(path);
        }
    }

    private static String getCanonicalQueryString() {
        return "";
    }

    private static String getCanonicalHeaders(String signedHeaders, Map<String, String> canonicalHeaders) {
        Set<String> headerNames = Sets.newHashSet(signedHeaders.split(";"));
        List<String> kvs = Lists.newArrayList();
        for (String key : headerNames) {
            final String canonicalKey = key.trim().toLowerCase();
            String value = canonicalHeaders.get(canonicalKey);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            kvs.add(normalize(canonicalKey + COLON + normalize(value.trim())));
        }
        Collections.sort(kvs);
        return String.join(LF, kvs);
    }

    private static String normalizePath(String path) {
        return normalize(path).replace("%2F", "/");
    }

    private static String normalize(String value) {
        try {
            StringBuilder builder = new StringBuilder();
            for (byte b : value.getBytes(UTF_8)) {
                if (URI_UNRESERVED_CHARACTERS.get(b & 0xFF)) {
                    builder.append((char) b);
                } else {
                    builder.append(PERCENT_ENCODED_STRINGS[b & 0xFF]);
                }
            }
            return builder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String parseToHex(String decimalSerialNo) {
        // 10进制的证书序列号一定大于10位
        if (StringUtils.isEmpty(decimalSerialNo) || 10 >= decimalSerialNo.length()) {
            return decimalSerialNo;
        }
        return Long.toHexString(Long.parseLong(decimalSerialNo));
    }

    private static byte[] decodeBase64(String input) {
        return Base64.decodeBase64(input);
    }

    private static BCECPrivateKey string2PrivateKey(String priKey) {
        try {
            return (BCECPrivateKey)KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME).generatePrivate(
                    new PKCS8EncodedKeySpec(decodeBase64(priKey)));
        } catch (Exception e) {
            throw new RuntimeException("ConfigProblem, IsvPrivateKey ParseFail, value:" + priKey + ", ex:", e);
        }
    }

    public static BCECPublicKey string2PublicKey(String pubKey) {
        try {
            return (BCECPublicKey) KeyFactory.getInstance("EC").generatePublic(
                    new X509EncodedKeySpec(decodeBase64(pubKey)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("ConfigProblem, YopPublicKey ParseFail, value:" + pubKey + ", ex:", e);
        }
    }

    private static final BitSet URI_UNRESERVED_CHARACTERS = new BitSet();
    private static final String[] PERCENT_ENCODED_STRINGS = new String[256];

    // Regex which matches any of the sequences that we need to fix up after URLEncoder.encode().
    // private static final Pattern ENCODED_CHARACTERS_PATTERN;
    static {
        /*
         * StringBuilder pattern = new StringBuilder();
         *
         * pattern .append(Pattern.quote("+")) .append("|") .append(Pattern.quote("*")) .append("|")
         * .append(Pattern.quote("%7E")) .append("|") .append(Pattern.quote("%2F"));
         *
         * ENCODED_CHARACTERS_PATTERN = Pattern.compile(pattern.toString());
         */
        for (int i = 'a'; i <= 'z'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        URI_UNRESERVED_CHARACTERS.set('-');
        URI_UNRESERVED_CHARACTERS.set('.');
        URI_UNRESERVED_CHARACTERS.set('_');
        URI_UNRESERVED_CHARACTERS.set('~');

        for (int i = 0; i < PERCENT_ENCODED_STRINGS.length; ++i) {
            PERCENT_ENCODED_STRINGS[i] = String.format("%%%02X", i);
        }
    }


}
