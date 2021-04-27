/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcess;
import com.yeepay.yop.sdk.exception.VerifySignFailedException;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.CharacterConstants;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/4/27 9:44 上午
 */
public abstract class YopBaseSigner implements YopSigner {

    @Override
    public void checkSignature(YopHttpResponse httpResponse, String signature, YopCredentials credentials, SignOptions options) {
        String content = httpResponse.readContent();
        PKICredentialsItem pkiCredentialsItem = (PKICredentialsItem) credentials.getCredential();
        content = content.replaceAll("[ \t\n]", CharacterConstants.EMPTY);
        if (!signerProcessMap.get(pkiCredentialsItem.getCertType()).verify(content, signature, pkiCredentialsItem)) {
            throw new VerifySignFailedException("response sign verify failure");
        }
    }

    @Override
    public void registerYopSignProcess(CertTypeEnum certTypeEnum, YopSignProcess yopSignProcess) {
        signerProcessMap.put(certTypeEnum, yopSignProcess);
    }

    @Override
    public YopSignProcess getSignProcess(CertTypeEnum certType) {
        return signerProcessMap.get(certType);
    }
}
