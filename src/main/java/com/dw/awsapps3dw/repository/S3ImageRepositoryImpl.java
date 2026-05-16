package com.dw.awsapps3dw.repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.dw.awsapps3dw.config.AwsProperties;
import com.dw.awsapps3dw.domain.BucketContents;
import com.dw.awsapps3dw.domain.DirectoryEntry;
import com.dw.awsapps3dw.domain.StoredFile;
import com.dw.awsapps3dw.domain.StoredImage;
import com.dw.awsapps3dw.util.S3PathUtils;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Repository
public class S3ImageRepositoryImpl implements S3ImageRepository {

    private final S3Client s3Client;
    private final String bucketName;

    public S3ImageRepositoryImpl(S3Client s3Client, AwsProperties awsProperties) {
        this.s3Client = s3Client;
        this.bucketName = awsProperties.s3().bucketName();
    }

    @Override
    public StoredImage upload(
            String key,
            InputStream content,
            long contentLength,
            String contentType,
            String fileName) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(content, contentLength));

        HeadObjectResponse metadata = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());

        return new StoredImage(
                bucketName,
                key,
                fileName,
                metadata.contentLength(),
                metadata.contentType(),
                metadata.lastModified());
    }

    @Override
    public BucketContents listByPrefix(String prefix) {
        String normalizedPrefix = S3PathUtils.normalizePrefix(prefix);

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(normalizedPrefix)
                .delimiter("/")
                .build();

        List<DirectoryEntry> directories = new ArrayList<>();
        List<StoredFile> files = new ArrayList<>();

        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(request);

            response.commonPrefixes().forEach(commonPrefix -> {
                String path = commonPrefix.prefix();
                String name = path.substring(normalizedPrefix.length());
                if (name.endsWith("/")) {
                    name = name.substring(0, name.length() - 1);
                }
                directories.add(new DirectoryEntry(name, path));
            });

            for (S3Object object : response.contents()) {
                String key = object.key();
                if (key.equals(normalizedPrefix) || key.endsWith("/")) {
                    continue;
                }
                String name = key.substring(normalizedPrefix.length());
                files.add(new StoredFile(
                        name,
                        key,
                        object.size(),
                        object.eTag(),
                        object.lastModified()));
            }

            request = request.toBuilder().continuationToken(response.nextContinuationToken()).build();
        } while (response.isTruncated());

        directories.sort(Comparator.comparing(DirectoryEntry::name));
        files.sort(Comparator.comparing(StoredFile::name));

        return new BucketContents(bucketName, normalizedPrefix, directories, files);
    }
}
