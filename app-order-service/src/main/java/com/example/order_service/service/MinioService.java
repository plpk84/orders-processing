package com.example.order_service.service;

import com.example.order_lib.dto.AnalyticDto;
import com.example.order_service.config.properties.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @SneakyThrows
    public String saveToStorage(AnalyticDto analytic) {
        LocalDateTime currentDate = LocalDateTime.now();

        var pdfBytes = PdfDocumentService.createDocument(analytic, currentDate);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes)) {
            String fileName = "Отчет от " + currentDate + ".pdf";
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(fileName)
                            .stream(inputStream, pdfBytes.length, -1)
                            .contentType("application/pdf")
                            .build()
            );
            log.info(">> Отчет {} загружен в Minio", fileName);

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucket())
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error(">> Ошибка при экспорте отчета {}", e.getMessage());
            throw e;
        }
    }
}
