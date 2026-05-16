package com.dw.awsapps3dw.dto;

import java.util.List;

public record ListContentsResponse(
        String bucket,
        String prefix,
        List<DirectoryEntryDto> directories,
        List<FileEntryDto> files
) {}
