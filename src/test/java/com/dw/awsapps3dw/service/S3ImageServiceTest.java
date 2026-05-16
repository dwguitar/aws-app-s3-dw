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

import com.dw.awsapps3dw.config.AwsProperties;
import com.dw.awsapps3dw.domain.BucketContents;
import com.dw.awsapps3dw.domain.DirectoryEntry;
import com.dw.awsapps3dw.domain.StoredFile;
import com.dw.awsapps3dw.domain.StoredImage;
import com.dw.awsapps3dw.dto.ListContentsResponse;
import com.dw.awsapps3dw.dto.UploadImageResponse;
import com.dw.awsapps3dw.repository.S3ImageRepository;

@ExtendWith(MockitoExtension.class)
class S3ImageServiceTest {

    private static final String BUCKET = "aws-java-class-s3-863430399807-sa-east-1-an";
    private static final String DEFAULT_PREFIX = "Aula-Java/";

    @Mock
    private S3ImageRepository s3ImageRepository;

    private S3ImageService s3ImageService;

    @BeforeEach
    void setUp() {
        AwsProperties properties = new AwsProperties(
                "sa-east-1",
                new AwsProperties.S3Properties(BUCKET, DEFAULT_PREFIX));
        s3ImageService = new S3ImageServiceImpl(s3ImageRepository, new ImageContentValidator(), properties);
    }

    @Test
    void uploadImage_usesDefaultFolderWhenNotProvided() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                "image/png",
                "png".getBytes(StandardCharsets.UTF_8));

        when(s3ImageRepository.upload(
                        eq("Aula-Java/foto.png"),
                        any(),
                        eq(3L),
                        eq("image/png"),
                        eq("foto.png")))
                .thenReturn(new StoredImage(BUCKET, "Aula-Java/foto.png", "foto.png", 3L, "image/png", Instant.now()));

        UploadImageResponse response = s3ImageService.uploadImage(file, null);

        assertThat(response.key()).isEqualTo("Aula-Java/foto.png");
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
    void listContents_usesDefaultPrefixWhenNotProvided() {
        when(s3ImageRepository.listByPrefix("Aula-Java/", false))
                .thenReturn(new BucketContents(BUCKET, "Aula-Java/", false, List.of(), List.of()));

        ListContentsResponse response = s3ImageService.listContents(null, false);

        verify(s3ImageRepository).listByPrefix("Aula-Java/", false);
        assertThat(response.prefix()).isEqualTo("Aula-Java/");
        assertThat(response.bucketArn()).isEqualTo("arn:aws:s3:::" + BUCKET);
        assertThat(response.region()).isEqualTo("sa-east-1");
    }

    @Test
    void listAulaJavaFolder_listsConfiguredPrefix() {
        when(s3ImageRepository.listByPrefix("Aula-Java/", true))
                .thenReturn(new BucketContents(
                        BUCKET,
                        "Aula-Java/",
                        true,
                        List.of(),
                        List.of(new StoredFile(
                                "slides/aula1.pdf",
                                "Aula-Java/slides/aula1.pdf",
                                100L,
                                "\"abc\"",
                                Instant.parse("2026-05-16T10:00:00Z")))));

        ListContentsResponse response = s3ImageService.listAulaJavaFolder(true);

        assertThat(response.recursive()).isTrue();
        assertThat(response.totalFiles()).isEqualTo(1);
        assertThat(response.files().getFirst().key()).isEqualTo("Aula-Java/slides/aula1.pdf");
    }
}
