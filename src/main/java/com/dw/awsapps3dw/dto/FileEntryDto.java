package com.dw.awsapps3dw.dto;

import java.time.Instant;

public record FileEntryDto(
        String name,
        String key,
        long sizeBytes,
        String eTag,
        Instant uploadedAt
) {}
