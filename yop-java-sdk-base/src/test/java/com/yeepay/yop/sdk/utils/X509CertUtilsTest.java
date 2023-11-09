/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * title: X509CertUtilsTest<br>
 * description: <br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 7/19/22
 */
public class X509CertUtilsTest {

    @Test
    public void parseToHex() {
        Assert.assertEquals(X509CertUtils.parseToHex("275718169735"), "4032156487");
        Assert.assertEquals(X509CertUtils.parseToHex("290297451928"), "4397139598");
        Assert.assertEquals(X509CertUtils.parseToHex("275550212193"), "4028129061");
        Assert.assertEquals(X509CertUtils.parseToHex("275568425014"), "4029287836");
        Assert.assertEquals(X509CertUtils.parseToHex("289782695477"), "4378650635");
        Assert.assertEquals(X509CertUtils.parseToHex("289798445125"), "4379555845");
    }

    @Test
    public void parseToDecimal() {
        Assert.assertEquals(X509CertUtils.parseToDecimal("4059376239"), "276374708793");
        Assert.assertEquals(X509CertUtils.parseToDecimal("4032156487"), "275718169735");
        Assert.assertEquals(X509CertUtils.parseToDecimal("4397139598"), "290297451928");
        Assert.assertEquals(X509CertUtils.parseToDecimal("4028129061"), "275550212193");
        Assert.assertEquals(X509CertUtils.parseToDecimal("4029287836"), "275568425014");
        Assert.assertEquals(X509CertUtils.parseToDecimal("4378650635"), "289782695477");
        Assert.assertEquals(X509CertUtils.parseToDecimal("4379555845"), "289798445125");
    }
}