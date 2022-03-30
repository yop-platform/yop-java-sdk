package com.yeepay.yop.sdk.auth.signer;

import com.yeepay.yop.sdk.security.SignerTypeEnum;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2021/12/22 22:12
 */
public class YopSignerFactoryTest {

    @Test
    public void getSigner() {
        YopSigner yopSigner = YopSignerFactory.getSigner(SignerTypeEnum.OAUTH2);
        assertNotNull(yopSigner);
    }
}