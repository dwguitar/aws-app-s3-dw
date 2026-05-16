package com.dw.awsapps3dw.repository;

import java.io.InputStream;

import com.dw.awsapps3dw.domain.BucketContents;
import com.dw.awsapps3dw.domain.StoredImage;

public interface S3ImageRepository {

    StoredImage upload(String key, InputStream content, long contentLength, String contentType, String fileName);

    BucketContents listByPrefix(String prefix, boolean recursive);
}
