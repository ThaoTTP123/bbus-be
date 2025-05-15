package com.fpt.bbusbe.service;

import java.io.InputStream;
import java.time.Duration;

public interface S3Service {

    void uploadFile(String key, InputStream inputStream, long contentLength, String contentType);

    void deleteFile(String key);

    String generatePresignedUrl(String key);
}
