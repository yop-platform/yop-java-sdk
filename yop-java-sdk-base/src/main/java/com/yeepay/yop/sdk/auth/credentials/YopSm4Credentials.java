/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

/**
 * title: SM4凭证<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/14
 */
public class YopSm4Credentials implements YopCredentials<String> {

    private String appKey;

    private String secretKey;

    public YopSm4Credentials(String appKey, String secretKey) {
        this.appKey = appKey;
        this.secretKey = secretKey;
    }

    public YopSm4Credentials(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String getAppKey() {
        return this.appKey;
    }

    @Override
    public String getCredential() {
        return secretKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof YopSm4Credentials) {
            YopSm4Credentials other = (YopSm4Credentials) obj;
            return this.secretKey.equals(other.secretKey);
        }
        return false;
    }
}
