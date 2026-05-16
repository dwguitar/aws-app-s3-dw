package com.dw.awsapps3dw.domain;

import java.util.List;

public record BucketContents(
        String bucket,
        String prefix,
        boolean recursive,
        List<DirectoryEntry> directories,
        List<StoredFile> files
) {}
