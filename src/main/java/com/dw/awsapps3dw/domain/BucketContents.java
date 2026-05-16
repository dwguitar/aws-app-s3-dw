package com.dw.awsapps3dw.domain;

import java.util.List;

public record BucketContents(
        String bucket,
        String prefix,
        List<DirectoryEntry> directories,
        List<StoredFile> files
) {}
