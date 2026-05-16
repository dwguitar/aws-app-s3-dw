package com.dw.awsapps3dw.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dw.awsapps3dw.config.AwsProperties;
import com.dw.awsapps3dw.domain.BucketContents;
import com.dw.awsapps3dw.domain.StoredImage;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
class S3ImageRepositoryImplTest {

    private static final String BUCKET = "aws-java-class-s3-863430399807-sa-east-1-an";

    @Mock
    private S3Client s3Client;

    private S3ImageRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        AwsProperties properties = new AwsProperties(
                "sa-east-1",
                new AwsProperties.S3Properties(BUCKET, "Aula-Java/"));
        repository = new S3ImageRepositoryImpl(s3Client, properties);
    }

    @Test
    void upload_putsObjectInAulaJavaFolder() {
        Instant uploadedAt = Instant.parse("2026-05-16T12:00:00Z");
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentLength(3L)
                .contentType("image/png")
                .lastModified(uploadedAt)
                .build());

        StoredImage result = repository.upload(
                "Aula-Java/foto.png",
                new ByteArrayInputStream("png".getBytes(StandardCharsets.UTF_8)),
                3L,
                "image/png",
                "foto.png");

        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(putCaptor.capture(), any(RequestBody.class));

        assertThat(putCaptor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(putCaptor.getValue().key()).isEqualTo("Aula-Java/foto.png");
        assertThat(result.key()).isEqualTo("Aula-Java/foto.png");
    }

    @Test
    void listByPrefix_listsAulaJavaWithDelimiter() {
        ArgumentCaptor<ListObjectsV2Request> listCaptor = ArgumentCaptor.forClass(ListObjectsV2Request.class);
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(ListObjectsV2Response.builder()
                        .commonPrefixes(CommonPrefix.builder().prefix("Aula-Java/modulo-1/").build())
                        .contents(
                                S3Object.builder()
                                        .key("Aula-Java/diagrama.png")
                                        .size(100L)
                                        .eTag("\"abc\"")
                                        .lastModified(Instant.parse("2026-05-16T10:00:00Z"))
                                        .build())
                        .isTruncated(false)
                        .build());

        BucketContents contents = repository.listByPrefix("Aula-Java/", false);

        verify(s3Client).listObjectsV2(listCaptor.capture());
        assertThat(listCaptor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(listCaptor.getValue().prefix()).isEqualTo("Aula-Java/");
        assertThat(listCaptor.getValue().delimiter()).isEqualTo("/");
        assertThat(contents.directories()).hasSize(1);
        assertThat(contents.directories().getFirst().name()).isEqualTo("modulo-1");
        assertThat(contents.files()).hasSize(1);
        assertThat(contents.files().getFirst().name()).isEqualTo("diagrama.png");
    }

    @Test
    void listByPrefix_recursive_listsAllObjectsUnderPrefix() {
        ArgumentCaptor<ListObjectsV2Request> listCaptor = ArgumentCaptor.forClass(ListObjectsV2Request.class);
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(ListObjectsV2Response.builder()
                        .contents(
                                S3Object.builder()
                                        .key("Aula-Java/modulo-1/aula.pdf")
                                        .size(200L)
                                        .eTag("\"def\"")
                                        .lastModified(Instant.parse("2026-05-16T11:00:00Z"))
                                        .build())
                        .isTruncated(false)
                        .build());

        BucketContents contents = repository.listByPrefix("Aula-Java/", true);

        verify(s3Client).listObjectsV2(listCaptor.capture());
        assertThat(listCaptor.getValue().delimiter()).isNull();
        assertThat(contents.recursive()).isTrue();
        assertThat(contents.directories()).isEmpty();
        assertThat(contents.files()).hasSize(1);
        assertThat(contents.files().getFirst().name()).isEqualTo("modulo-1/aula.pdf");
    }
}
