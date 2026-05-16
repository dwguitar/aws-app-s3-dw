package com.dw.awsapps3dw.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dw.awsapps3dw.dto.ListContentsResponse;
import com.dw.awsapps3dw.dto.UploadImageResponse;
import com.dw.awsapps3dw.service.S3ImageService;

@RestController
@RequestMapping(path = "/api/s3", produces = MediaType.APPLICATION_JSON_VALUE)
public class S3Controller {

    private final S3ImageService s3ImageService;

    public S3Controller(S3ImageService s3ImageService) {
        this.s3ImageService = s3ImageService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadImageResponse uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) throws IOException {
        return s3ImageService.uploadImage(file, folder);
    }

    @GetMapping("/contents")
    public ListContentsResponse listContents(
            @RequestParam(value = "prefix", required = false, defaultValue = "") String prefix) {
        return s3ImageService.listContents(prefix);
    }
}
