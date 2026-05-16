package com.dw.awsapps3dw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.dw.awsapps3dw.domain.BucketContents;
import com.dw.awsapps3dw.domain.DirectoryEntry;
import com.dw.awsapps3dw.domain.StoredFile;
import com.dw.awsapps3dw.domain.StoredImage;
import com.dw.awsapps3dw.repository.S3ImageRepository;
import com.dw.awsapps3dw.dto.ListContentsResponse;
import com.dw.awsapps3dw.dto.UploadImageResponse;

@ExtendWith(MockitoExtension.class)
class S3ImageServiceTest {

    private static final String BUCKET = "s3-bucket-app-dw-1";

    @Mock
    private S3ImageRepository s3ImageRepository;

    private S3ImageService s3ImageService;

    @BeforeEach
    void setUp() {
        s3ImageService = new S3ImageServiceImpl(s3ImageRepository, new ImageContentValidator());
    }

    @Test
    void uploadImage_validatesAndDelegatesToRepository() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                "image/png",
                "png".getBytes(StandardCharsets.UTF_8));

        Instant uploadedAt = Instant.parse("2026-05-16T12:00:00Z");
        when(s3ImageRepository.upload(
                        eq("imagens/foto.png"),
                        any(),
                        eq(3L),
                        eq("image/png"),
                        eq("foto.png")))
                .thenReturn(new StoredImage(BUCKET, "imagens/foto.png", "foto.png", 3L, "image/png", uploadedAt));

        UploadImageResponse response = s3ImageService.uploadImage(file, "imagens");

        verify(s3ImageRepository).upload(
                eq("imagens/foto.png"), any(), eq(3L), eq("image/png"), eq("foto.png"));
        assertThat(response.key()).isEqualTo("imagens/foto.png");
        assertThat(response.uploadedAt()).isEqualTo(uploadedAt);
    }

    @Test
    void uploadImage_rejectsNonImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "doc.pdf",
                "application/pdf",
                "data".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> s3ImageService.uploadImage(file, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imagem");
    }

    @Test
    void listContents_mapsRepositoryResultToDto() {
        when(s3ImageRepository.listByPrefix("imagens/"))
                .thenReturn(new BucketContents(
                        BUCKET,
                        "imagens/",
                        List.of(new DirectoryEntry("2026", "imagens/2026/")),
                        List.of(new StoredFile(
                                "foto.png",
                                "imagens/foto.png",
                                100L,
                                "\"abc\"",
                                Instant.parse("2026-05-16T10:00:00Z")))));

        ListContentsResponse response = s3ImageService.listContents("imagens/");

        assertThat(response.bucket()).isEqualTo(BUCKET);
        assertThat(response.directories()).hasSize(1);
        assertThat(response.directories().getFirst().name()).isEqualTo("2026");
        assertThat(response.files()).hasSize(1);
        assertThat(response.files().getFirst().name()).isEqualTo("foto.png");
    }
}
