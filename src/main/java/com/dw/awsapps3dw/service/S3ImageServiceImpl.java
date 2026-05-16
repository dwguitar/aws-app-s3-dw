package com.dw.awsapps3dw.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dw.awsapps3dw.domain.BucketContents;
import com.dw.awsapps3dw.domain.DirectoryEntry;
import com.dw.awsapps3dw.domain.StoredFile;
import com.dw.awsapps3dw.domain.StoredImage;
import com.dw.awsapps3dw.repository.S3ImageRepository;
import com.dw.awsapps3dw.util.S3PathUtils;
import com.dw.awsapps3dw.dto.DirectoryEntryDto;
import com.dw.awsapps3dw.dto.FileEntryDto;
import com.dw.awsapps3dw.dto.ListContentsResponse;
import com.dw.awsapps3dw.dto.UploadImageResponse;

@Service
public class S3ImageServiceImpl implements S3ImageService {

    private final S3ImageRepository s3ImageRepository;
    private final ImageContentValidator imageContentValidator;

    public S3ImageServiceImpl(S3ImageRepository s3ImageRepository, ImageContentValidator imageContentValidator) {
        this.s3ImageRepository = s3ImageRepository;
        this.imageContentValidator = imageContentValidator;
    }

    @Override
    public UploadImageResponse uploadImage(MultipartFile file, String folder) throws IOException {
        imageContentValidator.validate(file);

        String objectKey = S3PathUtils.buildObjectKey(folder, file.getOriginalFilename());

        StoredImage storedImage = s3ImageRepository.upload(
                objectKey,
                file.getInputStream(),
                file.getSize(),
                file.getContentType(),
                file.getOriginalFilename());

        return toUploadResponse(storedImage);
    }

    @Override
    public ListContentsResponse listContents(String prefix) {
        BucketContents contents = s3ImageRepository.listByPrefix(prefix);
        return toListResponse(contents);
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

    private static ListContentsResponse toListResponse(BucketContents contents) {
        return new ListContentsResponse(
                contents.bucket(),
                contents.prefix(),
                contents.directories().stream().map(S3ImageServiceImpl::toDirectoryDto).toList(),
                contents.files().stream().map(S3ImageServiceImpl::toFileDto).toList());
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
