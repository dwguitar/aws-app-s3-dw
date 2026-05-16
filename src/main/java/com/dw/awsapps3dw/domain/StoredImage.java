package com.dw.awsapps3dw.domain;

import java.time.Instant;

public record StoredImage(
        String bucket,
        String key,
        String fileName,
        long sizeBytes,
        String contentType,
        Instant uploadedAt
) {}
