package com.example.order_service.controller;

import com.example.order_service.service.AnalyticService;
import com.example.order_service.service.MinioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics")
@Tag(name = "Order")
public class AnalyticController {
    private final MinioService minioService;
    private final AnalyticService analyticService;

    @GetMapping
    @SneakyThrows
    public ResponseEntity<Map<String, Object>> getAnalytic() {
        var analytic = analyticService.getAnalytic();
        String url = minioService.saveToStorage(analytic);
        return ResponseEntity.ok(
                Map.of(
                        "analytic", analytic,
                        "pdf", url
                )
        );
    }
}
