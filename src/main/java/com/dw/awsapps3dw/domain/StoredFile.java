package com.dw.awsapps3dw.domain;

import java.time.Instant;

public record StoredFile(
        String name,
        String key,
        long sizeBytes,
        String eTag,
        Instant uploadedAt
) {}
