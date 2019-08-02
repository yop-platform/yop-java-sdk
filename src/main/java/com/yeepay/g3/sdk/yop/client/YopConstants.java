package com.yeepay.g3.sdk.yop.client;

import com.google.common.base.Joiner;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopConstants {

    // Initialize DEFAULT_USER_AGENT
    static {
        String language = System.getProperty("user.language");
        if (language == null) {
            language = "";
        }
        String region = System.getProperty("user.region");
        if (region == null) {
            region = "";
        }
        USER_AGENT = Joiner.on('/').join(YopConstants.CLIENT_LANGS, YopConstants.CLIENT_VERSION, System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.version"),
                System.getProperty("java.version"), language, region)
                .replace(' ', '_');
    }

    public static final String CLIENT_VERSION = "3.2.17";
    public static final String CLIENT_LANGS = "java";

    public static String USER_AGENT;

    public static final String ENCODING = "UTF-8";

    public static final String SUCCESS = "SUCCESS";

    // 方法的默认参数名
    public static final String METHOD = "method";

    // 格式化默认参数名
    public static final String FORMAT = "format";

    // 本地化默认参数名
    public static final String LOCALE = "locale";

    // 会话id默认参数名
    public static final String SESSION_ID = "sessionId";

    // 应用键的默认参数名 ;
    public static final String APP_KEY = "appKey";

    // 服务版本号的默认参数名
    public static final String VERSION = "v";

    // 签名的默认参数名
    public static final String SIGN = "sign";

    // 返回结果是否签名
    public static final String SIGN_RETURN = "signRet";

    // 商户编号
    public static final String CUSTOMER_NO = "customerNo";

    // 加密报文key
    public static final String ENCRYPT = "encrypt";

    // 时间戳
    public static final String TIMESTAMP = "ts";

    // 保护参数
    public static final String[] PROTECTED_KEY = {APP_KEY, VERSION, SIGN, METHOD, FORMAT, LOCALE,
            SESSION_ID, CUSTOMER_NO, ENCRYPT, SIGN_RETURN, TIMESTAMP};

    public static final String ALG_MD5 = "MD5";
    public static final String ALG_AES = "AES";
    public static final String ALG_SHA = "SHA";
    public static final String ALG_SHA1 = "SHA1";
    public static final String ALG_SHA256 = "SHA256";

    public static final String DEFAULT_SERVER_ROOT = "https://openapi.yeepay.com/yop-center";

    public static final String DEFAULT_YOS_SERVER_ROOT = "https://yos.yeepay.com/yop-center";

    public static final String DEFAULT_SANDBOX_SERVER_ROOT = "https://sandbox.yeepay.com/yop-center";

    public static final String[] API_URI_PREFIX = {"/rest/v", "/yos/v"};

    public static final String SANDBOX_GATEWAY_VIA = "sandbox";

    /**
     * 判断是否为保护参数
     *
     * @param key
     * @return
     */
    public static boolean isProtectedKey(String key) {
        for (String k : YopConstants.PROTECTED_KEY) {
            if (k.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
