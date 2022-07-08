/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

/**
 * title: Yop平台凭证(一般为server端证书，用于加密、验签)<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2/4/21 5:26 PM
 */
public interface YopPlatformCredentials extends YopCredentials<CredentialsItem>, CertificateCredentials {
}
