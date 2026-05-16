package com.dw.awsapps3dw.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dw.awsapps3dw.config.AwsProperties;
import com.dw.awsapps3dw.domain.BucketContents;
import com.dw.awsapps3dw.domain.DirectoryEntry;
import com.dw.awsapps3dw.domain.StoredFile;
import com.dw.awsapps3dw.domain.StoredImage;
import com.dw.awsapps3dw.dto.DirectoryEntryDto;
import com.dw.awsapps3dw.dto.FileEntryDto;
import com.dw.awsapps3dw.dto.ListContentsResponse;
import com.dw.awsapps3dw.dto.UploadImageResponse;
import com.dw.awsapps3dw.repository.S3ImageRepository;
import com.dw.awsapps3dw.util.S3PathUtils;

@Service
public class S3ImageServiceImpl implements S3ImageService {

    private static final String AULA_JAVA_PREFIX = "Aula-Java/";

    private final S3ImageRepository s3ImageRepository;
    private final ImageContentValidator imageContentValidator;
    private final AwsProperties awsProperties;

    public S3ImageServiceImpl(
            S3ImageRepository s3ImageRepository,
            ImageContentValidator imageContentValidator,
            AwsProperties awsProperties) {
        this.s3ImageRepository = s3ImageRepository;
        this.imageContentValidator = imageContentValidator;
        this.awsProperties = awsProperties;
    }

    @Override
    public UploadImageResponse uploadImage(MultipartFile file, String folder) throws IOException {
        imageContentValidator.validate(file);

        String targetFolder = S3PathUtils.resolveUploadFolder(folder, awsProperties.s3().defaultPrefix());
        String objectKey = S3PathUtils.buildObjectKey(targetFolder, file.getOriginalFilename());

        StoredImage storedImage = s3ImageRepository.upload(
                objectKey,
                file.getInputStream(),
                file.getSize(),
                file.getContentType(),
                file.getOriginalFilename());

        return toUploadResponse(storedImage);
    }

    @Override
    public ListContentsResponse listContents(String prefix, boolean recursive) {
        String effectivePrefix = S3PathUtils.resolveListingPrefix(prefix, awsProperties.s3().defaultPrefix());
        BucketContents contents = s3ImageRepository.listByPrefix(effectivePrefix, recursive);
        return toListResponse(contents);
    }

    @Override
    public ListContentsResponse listBucketRoot() {
        BucketContents contents = s3ImageRepository.listByPrefix("", false);
        return toListResponse(contents);
    }

    @Override
    public ListContentsResponse listAulaJavaFolder(boolean recursive) {
        BucketContents contents = s3ImageRepository.listByPrefix(AULA_JAVA_PREFIX, recursive);
        return toListResponse(contents);
    }

    private ListContentsResponse toListResponse(BucketContents contents) {
        return new ListContentsResponse(
                contents.bucket(),
                awsProperties.s3().bucketArn(),
                awsProperties.region(),
                contents.prefix(),
                contents.recursive(),
                contents.directories().size(),
                contents.files().size(),
                contents.directories().stream().map(S3ImageServiceImpl::toDirectoryDto).toList(),
                contents.files().stream().map(S3ImageServiceImpl::toFileDto).toList());
    }

    private static UploadImageResponse toUploadResponse(StoredImage storedImage) {
        return new UploadImageResponse(
                storedImage.bucket(),
                storedImage.key(),
                storedImage.fileName(),
                storedImage.sizeBytes(),
                storedImage.contentType(),
                storedImage.uploadedAt());
    }

    private static DirectoryEntryDto toDirectoryDto(DirectoryEntry entry) {
        return new DirectoryEntryDto(entry.name(), entry.path());
    }

    private static FileEntryDto toFileDto(StoredFile file) {
        return new FileEntryDto(
                file.name(),
                file.key(),
                file.sizeBytes(),
                file.eTag(),
                file.uploadedAt());
    }
}
