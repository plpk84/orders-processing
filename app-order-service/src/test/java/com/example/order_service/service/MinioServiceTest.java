package com.example.order_service.service;

import com.example.order_lib.dto.AnalyticDto;
import com.example.order_service.config.properties.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private MinioService minioService;

    private static final String TEST_BUCKET_NAME = "test-bucket";
    private static final String TEST_URL = "http://test-url.com/document.pdf";

    @BeforeEach
    void setUp() {
        when(minioProperties.getBucket()).thenReturn(TEST_BUCKET_NAME);
    }

    @Test
    @SneakyThrows
    void testSaveToStorage_Success() {
        //given
        AnalyticDto analyticDto = new AnalyticDto(0L, null, null, null);
        byte[] testPdfBytes = "test pdf content".getBytes();

        // when && then
        try (var mockedPdfService = mockStatic(PdfDocumentService.class)) {
            mockedPdfService.when(() -> PdfDocumentService.createDocument(any(AnalyticDto.class), any(LocalDateTime.class)))
                    .thenReturn(testPdfBytes);

            when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenReturn(TEST_URL);

            String resultUrl = minioService.saveToStorage(analyticDto);

            assertNotNull(resultUrl);
            assertEquals(TEST_URL, resultUrl);

            verify(minioClient).putObject(any(PutObjectArgs.class));
            verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
        }
    }
}
