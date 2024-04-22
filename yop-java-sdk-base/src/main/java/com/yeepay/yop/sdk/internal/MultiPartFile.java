package com.yeepay.yop.sdk.internal;

import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.checksum.CRC64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Pattern;
import java.util.zip.CheckedInputStream;

/**
 * title: MultiPartFile<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/12/27 11:40
 */
public class MultiPartFile implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPartFile.class);

    private static final long serialVersionUID = -1L;

    private static final int EXT_READ_BUFFER_SIZE = 64 * 1024;

    private static final Pattern FILE_EXT_NAME_PATTERN = Pattern.compile("^\\.[^.\\s]+");

    private final CheckedInputStream inputStream;

    private final String fileName;

    public MultiPartFile(File file) throws IOException {
        this(new FileInputStream(file), file.getName(), true);
    }

    public MultiPartFile(InputStream in) throws IOException {
        Pair<String, CheckedInputStream> inputStreamPair = getCheckedInputStreamPair(in);
        this.fileName = FileUtils.randomFileName(8, inputStreamPair.getLeft());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("file autoDetect, auto:{}", this.fileName);
        }
        this.inputStream = inputStreamPair.getRight();
    }

    public MultiPartFile(InputStream in, String originFileName) throws IOException {
        this(in, originFileName, false);
    }

    public MultiPartFile(InputStream in, String originFileName, boolean autoDetect) throws IOException {
        if (autoDetect) {
            final Pair<String, CheckedInputStream> checkedInputStreamPair = getCheckedInputStreamPair(in);
            final String autoFileExtName = checkedInputStreamPair.getLeft();
            if (null != autoFileExtName && FILE_EXT_NAME_PATTERN.matcher(autoFileExtName).matches()) {
                this.fileName = StringUtils.substringBeforeLast(originFileName, ".") + autoFileExtName;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.info("file autoDetect, origin:{}, auto:{}", originFileName, this.fileName);
                }
            } else {
                this.fileName = originFileName;
            }
            this.inputStream = checkedInputStreamPair.getRight();
        } else {
            this.fileName = originFileName;
            this.inputStream = getCheckedInputStream(in);
        }
    }

    public MultiPartFile(String fileExtName, InputStream in) throws IOException {
        this.fileName = FileUtils.randomFileName(8, fileExtName);
        this.inputStream = getCheckedInputStream(in);
    }

    public CheckedInputStream getInputStream() {
        return inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    private static Pair<String, CheckedInputStream> getCheckedInputStreamPair(InputStream inputStream) throws IOException {
        if (inputStream instanceof FileInputStream) {
            MarkableFileInputStream in = new MarkableFileInputStream((FileInputStream) inputStream);
            in.mark(0);
            //解析文件扩展名的时候会读取流的前64*1024个字节,需要reset文件流
            String fileExtName = FileUtils.getFileExt(in);
            in.reset();
            return new ImmutablePair<String, CheckedInputStream>(fileExtName, getCheckedInputStream(in));
        }
        //解析文件扩展名的时候会读取流的前64*1024个字节
        byte[] extReadBuffer = new byte[EXT_READ_BUFFER_SIZE];
        int totalRead = 0;
        int lastRead = inputStream.read(extReadBuffer);
        while (lastRead != -1) {
            totalRead += lastRead;
            if (totalRead == EXT_READ_BUFFER_SIZE) {
                break;
            }
            lastRead = inputStream.read(extReadBuffer, totalRead, EXT_READ_BUFFER_SIZE - totalRead);
        }
        ByteArrayInputStream extReadIn = new ByteArrayInputStream(extReadBuffer, 0, totalRead);
        String fileExtName = FileUtils.getFileExt(extReadIn);
        extReadIn.reset();
        SequenceInputStream sequenceInputStream = new SequenceInputStream(extReadIn, inputStream);
        return new ImmutablePair<String, CheckedInputStream>(fileExtName, getCheckedInputStream(sequenceInputStream));
    }

    private static CheckedInputStream getCheckedInputStream(InputStream inputStream) throws IOException {
        return new CheckedInputStream(inputStream, new CRC64());
    }
}
