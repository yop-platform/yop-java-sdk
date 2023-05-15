package com.yeepay.yop.sdk.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 2018/6/28 下午1:22
 */
public class FileUtils {

    public static String getFileName(InputStream file) {
        String fileName = "";
//        if (file instanceof FileInputStream) {
//             TODO
//            fileName = "";
//        } else {
        String fileExt = getFileExt(file);
        fileName = randomFileName(8, fileExt);
//        }
        return fileName;
    }

    public static String randomFileName(int len, String fileExt) {
        return System.currentTimeMillis() + "-yop-" + RandomStringUtils.randomAlphanumeric(len) + fileExt;
    }

    private static String getMimeType(File file) {
        String mimeType = "application/octet-stream";
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            mimeType = getMimeType(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return mimeType;
    }

    private static String getMimeType(InputStream stream) {
        String mimeType = "application/octet-stream";
        AutoDetectParser parser = new AutoDetectParser();
        parser.setParsers(new HashMap<MediaType, Parser>());

        Metadata metadata = new Metadata();

        try {
            ContentHandler contenthandler = new BodyContentHandler();
            parser.parse(stream, contenthandler, metadata);
            mimeType = metadata.get(HttpHeaders.CONTENT_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mimeType;
    }

    public static String getFileExt(File file) {
        String fileExt = ".bin";
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            fileExt = getFileExt(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return fileExt;
    }

    public static String getFileExt(InputStream stream) {
        String fileExt = ".bin";
        try {
            String mimeType = getMimeType(stream);
            fileExt = TikaConfig.getDefaultConfig().getMimeRepository().getRegisteredMimeType(mimeType).getExtension();
        } catch (MimeTypeException e) {
            e.printStackTrace();
        }
        return fileExt;
    }

}
