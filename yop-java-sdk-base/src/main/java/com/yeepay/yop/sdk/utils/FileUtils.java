package com.yeepay.yop.sdk.utils;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.model.yos.YosDownloadInputStream;
import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;

import static com.yeepay.yop.sdk.YopConstants.FILE_PROTOCOL_PREFIX;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2018/6/28 下午1:22
 */
public final class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    public static String getFileName(InputStream file) {
        String fileName;
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
            mimeType = getMimeType(stream);
        } catch (Exception e) {
            LOGGER.error("error when getMimeType, ex:", e);
        } finally {
            StreamUtils.closeQuietly(stream);
        }

        return mimeType;
    }

    private static String getMimeType(InputStream stream) {
        String mimeType = "application/octet-stream";
        AutoDetectParser parser = new AutoDetectParser();
        parser.setParsers(Maps.newHashMap());

        Metadata metadata = new Metadata();

        try {
            ContentHandler contenthandler = new BodyContentHandler();
            parser.parse(stream, contenthandler, metadata);
            mimeType = metadata.get(HttpHeaders.CONTENT_TYPE);
        } catch (Exception e) {
            LOGGER.error("error when getMimeType, ex:", e);
        }

        return mimeType;
    }

    public static String getFileExt(File file) {
        String fileExt = ".bin";
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            fileExt = getFileExt(stream);
        } catch (Exception e) {
            LOGGER.error("error when getFileExt, ex:", e);
        } finally {
            StreamUtils.closeQuietly(stream);
        }

        return fileExt;
    }

    public static String getFileExt(InputStream stream) {
        String fileExt = ".bin";
        try {
            String mimeType = getMimeType(stream);
            fileExt = TikaConfig.getDefaultConfig().getMimeRepository().getRegisteredMimeType(mimeType).getExtension();
        } catch (MimeTypeException e) {
            LOGGER.error("error when getFileExt, ex:", e);
        }
        return fileExt;
    }

    public static InputStream getResourceAsStream(String resource) {
        // 支持绝对路径
        if (StringUtils.startsWith(resource, FILE_PROTOCOL_PREFIX)) {
            resource = StringUtils.substring(resource, FILE_PROTOCOL_PREFIX.length());
        }
        File file = new File(resource);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                // ignore
            }
        }

        // 尝试从classpath加载
        while (StringUtils.startsWith(resource, "/")) {
            resource = StringUtils.substring(resource, 1);
        }
        return getContextClassLoader().getResourceAsStream(resource);
    }

    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 保存文件到本地
     *
     * @param yosDownloadResponse 接口返回
     * @return File
     */
    public static File saveFile(YosDownloadResponse yosDownloadResponse) {
        YosDownloadInputStream yosDownloadInputStream = yosDownloadResponse.getResult();
        try {
            String filePrefix = "yos-", fileSuffix = ".tmp";
            try {
                String fileName = getFileNameFromHeader(yosDownloadResponse.getMetadata().getContentDisposition());
                if (StringUtils.isNotBlank(fileName)) {
                    final String[] split = fileName.split("\\.");
                    if (split.length == 2) {
                        if (StringUtils.length(split[0])  > 3) {
                            filePrefix = split[0];
                        }
                        if (StringUtils.isNotBlank(split[1])) {
                            fileSuffix = CharacterConstants.DOT + split[1];
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("parse Content-Disposition fail, value:" + yosDownloadResponse.getMetadata().getContentDisposition(), e);
            }
            File tmpFile = File.createTempFile(filePrefix, fileSuffix);
            long size = IOUtils.copy(yosDownloadInputStream, Files.newOutputStream(tmpFile.toPath()));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("file downloaded to path:{}, size:{}.", tmpFile.getAbsolutePath(), size);
            }
            return tmpFile;
        } catch (Exception ex) {
            LOGGER.error("fail to save file, response:" + yosDownloadResponse, ex);
            throw new YopClientException("fail to save file, response:" + yosDownloadResponse);
        } finally {
            StreamUtils.closeQuietly(yosDownloadInputStream);
        }
    }

    private static String getFileNameFromHeader(String contentDisposition) {
        final String[] parts = contentDisposition.split( "filename=");
        if (parts.length == 2) {
            return StringUtils.trim(parts[1]);
        }
        return null;
    }

}
