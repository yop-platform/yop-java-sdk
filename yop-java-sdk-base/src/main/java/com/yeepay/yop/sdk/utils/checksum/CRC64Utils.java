package com.yeepay.yop.sdk.utils.checksum;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedLong;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.zip.CheckedInputStream;

/**
 * title: CRC64工具类<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/5/31 15:30
 */
public class CRC64Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CRC64Utils.class);

    public static String getCRC64(List<CheckedInputStream> inputStreams) {
        List<String> crc64s = Lists.newArrayListWithExpectedSize(inputStreams.size());
        for (CheckedInputStream in : inputStreams) {
            crc64s.add(UnsignedLong.fromLongBits(in.getChecksum().getValue()).toString());
        }
        return StringUtils.join(crc64s, "/");
    }
}
