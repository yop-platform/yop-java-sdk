package com.yeepay.yop.sdk.internal;

import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.checksum.CRC64;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
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

    private static final long serialVersionUID = -1L;

    private static final int EXT_READ_BUFFER_SIZE = 64 * 1024;

    private final CheckedInputStream inputStream;

    private final String fileName;

    public MultiPartFile(File file) throws IOException {
        this.inputStream = new CheckedInputStream(new FileInputStream(file), new CRC64());
        this.fileName = file.getName();
    }

    public MultiPartFile(InputStream in) throws IOException {
        Pair<String, CheckedInputStream> inputStreamPair;
        if (in instanceof FileInputStream) {
            inputStreamPair = getCheckedInputStreamPair((FileInputStream) in);
        } else {
            inputStreamPair = getCheckedInputStreamPair(in);
        }
        this.fileName = inputStreamPair.getLeft();
        this.inputStream = inputStreamPair.getRight();
    }

    public CheckedInputStream getInputStream() {
        return inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    private static Pair<String, CheckedInputStream> getCheckedInputStreamPair(FileInputStream fileInputStream) throws IOException {
        MarkableFileInputStream in = new MarkableFileInputStream(fileInputStream);
        in.mark(0);
        //解析文件扩展名的时候会读取流的前64*1024个字节,需要reset文件流
        String fileName = FileUtils.getFileName(in);
        in.reset();
        CheckedInputStream inputStream = new CheckedInputStream(in, new CRC64());
        return new ImmutablePair<String, CheckedInputStream>(fileName, inputStream);
    }

    private static Pair<String, CheckedInputStream> getCheckedInputStreamPair(InputStream inputStream) throws IOException {
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
        return new ImmutablePair<String, CheckedInputStream>(fileName, new CheckedInputStream(sequenceInputStream,
                new CRC64()));
    }
}
