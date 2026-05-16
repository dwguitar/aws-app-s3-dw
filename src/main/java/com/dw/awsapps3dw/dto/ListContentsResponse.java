package com.dw.awsapps3dw.dto;

import java.util.List;

public record ListContentsResponse(
        String bucket,
        String bucketArn,
        String region,
        String prefix,
        boolean recursive,
        int totalDirectories,
        int totalFiles,
        List<DirectoryEntryDto> directories,
        List<FileEntryDto> files
) {}
