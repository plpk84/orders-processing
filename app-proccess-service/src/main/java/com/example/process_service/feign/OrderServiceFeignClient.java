package com.example.process_service.feign;

import com.example.order_lib.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "order-service", url = "${spring.feign.client.order-service.url}")
public interface OrderServiceFeignClient {
    @PutMapping("/{id}")
    ResponseEntity<OrderDto> changeStatus(@PathVariable Long id);
}
