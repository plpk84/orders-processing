package com.example.order_service.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class MinioProperties {
    @Value("${storage.endpoint}")
    private String endpoint;
    @Value("${storage.username}")
    private String username;
    @Value("${storage.password}")
    private String password;
    @Value("${storage.region}")
    private String region;
    @Value("${storage.bucket}")
    private String bucket;
}
