package com.rinko.oss.service;

import java.io.InputStream;
import java.util.List;

public interface StorageService {

    void store(InputStream inputStream, String key, long contentLength, String contentType);

    InputStream getInputStream(String key);

    void delete(String key);

    String generatePresignUrl(String key, int expiresSeconds);

    String initiateMultipartUpload(String key, String contentType);

    String uploadPart(String key, String uploadId, int partNumber, InputStream data, long size);

    void completeMultipartUpload(String key, String uploadId, List<PartETag> parts);

    void abortMultipartUpload(String key, String uploadId);

    record PartETag(int partNumber, String etag) {
    }
}
