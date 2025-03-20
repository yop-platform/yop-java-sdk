package com.yeepay.yop.sdk.example;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.*;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
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

    private static final Logger LOGGER = LoggerFactory.getLogger( YopSm2CallbackExample.class);

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
        isvPrivateKeyMap.put("sandbox_sm_10080041523", string2PrivateKey("MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgr0mQ3/jjQOczWI6bnJFdqF4D/DFYHaqXftqXU/jGKpCgCgYIKoEcz1UBgi2hRANCAAQPpkZNnOnXTCXOIHJbfR+i6ea1QkM8HxkdO8KSWK8IgltHZxr5xlxiqR8inOREmmrxUQQagOH5i3oELWgXZz8G"));

        yopPublicKeyMap = new HashMap<>();
        // 此处key为16进制转换后的值，对应10进制的275568425014
        yopPublicKeyMap.put("4059376239", string2PublicKey("MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEStsxeNDxhJzM61Uy0rCmnW9Zs4Ze7oIKX27dgFTB7FsTsiCEzvxD7OTCKd7F17Xa1vpJ07C+2+H2OOFBSyadZA=="));

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

    // 注意：：：：：：：：：：：：：：：：：：：
    // 请求地址: 商户提供的回调地址
    // 回调地址格式为，https://xxx/{path}，eventType为path部分加上"/"前缀
    // 举例说明：假如回调地址为"https://callback.test/payNotify"，则eventType为"/payNotify"
    // 举例说明：假如回调地址为"https://callback.test"，则eventType为"/"
    private static String reqUri = "/yop-callback/cs_1720925143663";
    // 请求头
    private static Map<String, String> reqHeaders;

    // 注意：：：：：：：：：：：：：：：：：：：
    // 请求体：两侧的双引号要保留，代表json字符串
    private static String reqBody = "\"cdsnFDl8bVJSOuROSxNVDbLyxlDC0QGYvzZwTWxb0dMuTzDVwTCkATAmiiiUSfgOYoJlsz91qH6d1j0_TBdnSjN7EecCFZdo6MvatWIX5burxxLDAMAwo3QAmyiT5dVh72giQ-EIJAt8R_X5rO4PcA\"";

    static {
        reqHeaders = Maps.newHashMap();
        reqHeaders.put("Authorization", "YOP-SM2-SM3 yop-auth-v3/sandbox_sm_10080041523/2024-07-14T02:45:44Z/1800/content-length;content-type;x-yop-content-sm3;x-yop-encrypt;x-yop-request-id/GbzQbci-j9Qe2pC8lOwWejXo_1Cy8N5FD80xYKNhfX6d3LievqWPIanRLI8YL5UAb5AFJhcRpaax1BsxDQcHvA$SM3");
        reqHeaders.put("x-yop-content-sm3", "71a21d1557847e2865c9c5716490f16f4543686888cbdad6261afc015b4f91f7");
        reqHeaders.put("x-yop-encrypt", "yop-encrypt-v1//SM4_CBC_PKCS5Padding/BCbsJljzCjgpg7x1zK4z-EexCpADSpw0mSQL0ZXKOxyRDh_XSr_KUj7tX7GzJ-ouns1oT8MTrX76FYCp3pPQ-LElGwmvl-L3OpcaCP5ou9ABIH0zDrD8bCgm9eSLqiVLv_Jli6zh9VcWFsI-AZboUQo/4x2G0lyi51StUZjfPradeQ;eW9w/stream//JA");
        reqHeaders.put("x-yop-request-id", "cs_1720925143663");
        reqHeaders.put("x-yop-sign-serial-no", "4059376239");
        reqHeaders.put("x-yop-appkey", "sandbox_sm_10080041523");
        reqHeaders.put("Content-Type", "application/json");
        // 注意：：：：：：：：：：：：：：：：：：：
        // 请求体长度头需根据请求体手动计算，并补充到请求头中
        try {
            reqHeaders.put("Content-Length", reqBody.getBytes("UTF-8").length + "");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
       boolean verifySign = verifySign(canonicalReqString, signature, yopPublicKeyMap.get(platformSerialNo));
        LOGGER.info("验签结果：{}", verifySign);

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
        LOGGER.info("解密结果：{}", bizContent);

        // 构造应答报文
//        String respBody = "{\"result\":\"SUCCESS\"}";
        //HttpServletResponse resp;
//        resp.addHeader("x-yop-sign", encodeUrlSafeBase64(sign(respBody.getBytes(UTF_8))));
//        // 注意：此处为测试环境国密证书，生产环境请联系技术支持获取
//        resp.addHeader("x-yop-sign-serial-no", "4059376239");
//        resp.setContentType("application/json");
//        resp.getOutputStream().write(respBody.getBytes(UTF_8));
    }

    private static byte[] sign(byte[] data, String appKey) {
        BCECPrivateKey isvPrivateKey = isvPrivateKeyMap.get(appKey);
        try {
            ECParameterSpec parameterSpec = isvPrivateKey.getParameters();
            ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                    parameterSpec.getN(), parameterSpec.getH());
            ECPrivateKeyParameters priKeyParameters = new ECPrivateKeyParameters(isvPrivateKey.getD(), domainParameters);
            //der编码后的签名值
            byte[] derSign = sign(priKeyParameters, null, data);

            //der解码过程
            ASN1Sequence as = DERSequence.getInstance(derSign);
            byte[] rBytes = ((ASN1Integer) as.getObjectAt(0)).getValue().toByteArray();
            byte[] sBytes = ((ASN1Integer) as.getObjectAt(1)).getValue().toByteArray();
            //由于大数的补0规则，所以可能会出现33个字节的情况，要修正回32个字节
            rBytes = fixToCurveLengthBytes(rBytes);
            sBytes = fixToCurveLengthBytes(sBytes);
            byte[] rawSign = new byte[rBytes.length + sBytes.length];
            System.arraycopy(rBytes, 0, rawSign, 0, rBytes.length);
            System.arraycopy(sBytes, 0, rawSign, rBytes.length, sBytes.length);
            return rawSign;
        } catch (Exception e) {
            throw new RuntimeException("SystemError, Sign Fail, key:" + isvPrivateKey + ", ex:", e);
        }
    }

    /**
     * 签名
     *
     * @param priKeyParameters 私钥
     * @param withId           可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @param srcData          源数据
     * @return DER编码后的签名值
     * @throws CryptoException
     */
    public static byte[] sign(ECPrivateKeyParameters priKeyParameters, byte[] withId, byte[] srcData)
            throws CryptoException {
        SM2Signer signer = new SM2Signer();
        CipherParameters param;
        ParametersWithRandom pwr = new ParametersWithRandom(priKeyParameters, new SecureRandom());
        if (withId != null) {
            param = new ParametersWithID(pwr, withId);
        } else {
            param = pwr;
        }
        signer.init(true, param);
        signer.update(srcData, 0, srcData.length);
        return signer.generateSignature();
    }

    public static final SM2P256V1Curve CURVE = new SM2P256V1Curve();
    public final static BigInteger SM2_ECC_N = CURVE.getOrder();
    public final static BigInteger SM2_ECC_H = CURVE.getCofactor();
    public final static BigInteger SM2_ECC_GX = new BigInteger(
            "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
    public final static BigInteger SM2_ECC_GY = new BigInteger(
            "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);
    public static final ECPoint G_POINT = CURVE.createPoint(SM2_ECC_GX, SM2_ECC_GY);
    public static final ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(CURVE, G_POINT,
            SM2_ECC_N, SM2_ECC_H);
    public static final int CURVE_LEN = getCurveLength(DOMAIN_PARAMS);
    public static int getCurveLength(ECDomainParameters domainParams) {
        return (domainParams.getCurve().getFieldSize() + 7) / 8;
    }

    private static byte[] fixToCurveLengthBytes(byte[] src) {
        if (src.length == CURVE_LEN) {
            return src;
        }

        byte[] result = new byte[CURVE_LEN];
        if (src.length > CURVE_LEN) {
            System.arraycopy(src, src.length - result.length, result, 0, result.length);
        } else {
            System.arraycopy(src, 0, result, result.length - src.length, src.length);
        }
        return result;
    }

    private static String encodeUrlSafeBase64(byte[] input) {
        return Base64.encodeBase64URLSafeString(input);
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
                byte[] ivBytes = decodeBase64(iv);
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

    private static String buildCanonicalReqString(String httpPath, Map<String, String> canonicalHeaders) throws UnsupportedEncodingException {
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
                .append("POST").append(LF)
                .append(canonicalURI).append(LF)
                .append(canonicalQueryString).append(LF)
                .append(canonicalHeader).toString();
    }

    private static boolean verifySign(String canonicalReqString, String signature, BCECPublicKey publicKey) {
        try {
            byte[] signData = decodeBase64(signature);
            byte[] srcData = canonicalReqString.getBytes(UTF_8);
            ECParameterSpec parameterSpec = publicKey.getParameters();
            ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                    parameterSpec.getN(), parameterSpec.getH());
            ECPublicKeyParameters pubKeyParameters = new ECPublicKeyParameters(publicKey.getQ(), domainParameters);
            return verify(pubKeyParameters, null, srcData, encodeSM2SignToDER(signData));
        } catch (IOException e) {
            throw new RuntimeException("UnexpectedError, VerifySign Fail, data:" +
                    canonicalReqString + ", sign:" + signature + ", key:" + publicKey + ", ex:", e);
        }
    }

    /**
     * 把64字节的纯R+S字节数组编码成DER编码
     *
     * @param rawSign 64字节数组形式的SM2签名值，前32字节为R，后32字节为S
     * @return DER编码后的SM2签名值
     * @throws IOException
     */
    private static byte[] encodeSM2SignToDER(byte[] rawSign) throws IOException {
        //要保证大数是正数
        BigInteger r = new BigInteger(1, extractBytes(rawSign, 0, 32));
        BigInteger s = new BigInteger(1, extractBytes(rawSign, 32, 32));
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));
        return new DERSequence(v).getEncoded(ASN1Encoding.DER);
    }

    private static byte[] extractBytes(byte[] src, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(src, offset, result, 0, result.length);
        return result;
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

    private static String getCanonicalHeaders(String signedHeaders, Map<String, String> canonicalHeaders) throws UnsupportedEncodingException {
        Set<String> headerNames = Sets.newHashSet(signedHeaders.split(";"));
        List<String> kvs = Lists.newArrayList();
        for (String key : headerNames) {
            final String canonicalKey = key.trim().toLowerCase();
            String value = canonicalHeaders.get(canonicalKey);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            kvs.add(normalize(canonicalKey) + COLON + normalize(value.trim()));
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


