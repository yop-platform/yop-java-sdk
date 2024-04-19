package com.yeepay.yop.sdk.internal;

import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.checksum.CRC64;
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

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("[^.]+\\.[^.\\s]+");

    private final CheckedInputStream inputStream;

    private final String fileName;

    public MultiPartFile(File file) throws IOException {
        this(new FileInputStream(file), file.getName());
    }

    public MultiPartFile(InputStream in) throws IOException {
        Pair<String, CheckedInputStream> inputStreamPair = getCheckedInputStreamPair(in);
        this.fileName = inputStreamPair.getLeft();
        this.inputStream = inputStreamPair.getRight();
    }

    public MultiPartFile(InputStream in, String originFileName) throws IOException {
        if (null != originFileName && FILE_NAME_PATTERN.matcher(originFileName).matches()) {
            this.fileName = originFileName;
            this.inputStream = getCheckedInputStream(in);
        } else {
            final Pair<String, CheckedInputStream> checkedInputStreamPair = getCheckedInputStreamPair(in);
            this.fileName = checkedInputStreamPair.getLeft();
            this.inputStream = checkedInputStreamPair.getRight();
            LOGGER.warn("illegal param fileName, origin:{}, tika:{}", originFileName, this.fileName);
        }
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
            String fileName = FileUtils.getFileName(in);
            in.reset();
            return new ImmutablePair<String, CheckedInputStream>(fileName, getCheckedInputStream(in));
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
        String fileName = FileUtils.getFileName(extReadIn);
        extReadIn.reset();
        SequenceInputStream sequenceInputStream = new SequenceInputStream(extReadIn, inputStream);
        return new ImmutablePair<String, CheckedInputStream>(fileName, getCheckedInputStream(sequenceInputStream));
    }

    private static CheckedInputStream getCheckedInputStream(InputStream inputStream) throws IOException {
        return new CheckedInputStream(inputStream, new CRC64());
    }
}
