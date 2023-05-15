package com.yeepay.yop.sdk.utils;

import com.google.common.primitives.UnsignedLong;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;
import com.yeepay.g3.core.yop.sdk.sample.utils.checksum.CRC64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.CheckedInputStream;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/3/26 16:09
 */
public class IOUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);


    public static String getCRCValue(File file) {
        try {
            CheckedInputStream checkedInputStream = new CheckedInputStream(new FileInputStream(file), new CRC64());
            while (checkedInputStream.read() != -1) {

            }
            return UnsignedLong.fromLongBits(checkedInputStream.getChecksum().getValue()).toString();
        } catch (Exception ex) {
            throw new YopClientException("Exception occurred when get CRCVALUE for File:" + file.getName());
        }
    }

}
