package com.rinko.oss.service;

import com.rinko.oss.config.OssProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;

@Service
@ConditionalOnProperty(name = "rinko.oss.storage-type", havingValue = "s3")
public class S3StorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3StorageService(OssProperties ossProperties) {
        OssProperties.S3 s3 = ossProperties.getS3();
        this.bucket = s3.getBucket();
        var credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey()));
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(s3.getEndpoint()))
                .credentialsProvider(credentials)
                .region(Region.of(s3.getRegion()))
                .forcePathStyle(true)
                .build();
        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(s3.getEndpoint()))
                .credentialsProvider(credentials)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .checksumValidationEnabled(false)
                        .build())
                .region(Region.of(s3.getRegion()))
                .build();
    }

    @Override
    public void store(InputStream inputStream, String key, long contentLength, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket).key(key).contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
        log.debug("Stored S3 object: {}", key);
    }

    @Override
    public InputStream getInputStream(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket).key(key).build();
        return s3Client.getObject(request);
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    @Override
    public String generatePresignUrl(String key, int expiresSeconds) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
                .signatureDuration(Duration.ofSeconds(expiresSeconds))
                .build();
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }

    @Override
    public String initiateMultipartUpload(String key, String contentType) {
        CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
                .bucket(bucket).key(key).contentType(contentType).build();
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);
        return response.uploadId();
    }

    @Override
    public String uploadPart(String key, String uploadId, int partNumber, InputStream data, long size) {
        UploadPartRequest request = UploadPartRequest.builder()
                .bucket(bucket).key(key).uploadId(uploadId).partNumber(partNumber)
                .build();
        UploadPartResponse response = s3Client.uploadPart(request, RequestBody.fromInputStream(data, size));
        return response.eTag();
    }

    @Override
    public void completeMultipartUpload(String key, String uploadId, List<PartETag> parts) {
        List<CompletedPart> completedParts = parts.stream()
                .map(p -> CompletedPart.builder().partNumber(p.partNumber()).eTag(p.etag()).build())
                .toList();
        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                .bucket(bucket).key(key).uploadId(uploadId)
                .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
                .build();
        s3Client.completeMultipartUpload(request);
    }

    @Override
    public void abortMultipartUpload(String key, String uploadId) {
        s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                .bucket(bucket).key(key).uploadId(uploadId).build());
    }
}
