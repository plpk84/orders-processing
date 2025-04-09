package com.example.process_service.feign;

import com.example.order_lib.dto.OrderCreatedEventDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "order-service", url = "${spring.feign.client.order-service.url}")
public interface OrderServiceFeignClient {
    @PutMapping("/{id}")
    OrderCreatedEventDto changeStatus(@PathVariable Long id);
}
