package com.dw.awsapps3dw.dto;

import java.time.Instant;

public record UploadImageResponse(
        String bucket,
        String key,
        String fileName,
        long sizeBytes,
        String contentType,
        Instant uploadedAt
) {}
