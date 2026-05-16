package com.dw.awsapps3dw.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.dw.awsapps3dw.dto.ListContentsResponse;
import com.dw.awsapps3dw.dto.UploadImageResponse;

public interface S3ImageService {

    UploadImageResponse uploadImage(MultipartFile file, String folder) throws IOException;

    ListContentsResponse listContents(String prefix);
}
