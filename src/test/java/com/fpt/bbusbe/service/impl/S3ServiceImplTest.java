package com.fpt.bbusbe.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class S3ServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3ServiceImpl s3Service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadFile() {
        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
        s3Service.uploadFile("test-key", inputStream, 9, "text/plain");

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testDeleteFile() {
        s3Service.deleteFile("test-key");

        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void testGeneratePresignedUrl() {
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(mock(PresignedGetObjectRequest.class));

        String url = s3Service.generatePresignedUrl("test-key");

        assertNotNull(url);
        verify(s3Presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
    }
}
