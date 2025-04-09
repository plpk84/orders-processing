package com.example.process_service.handler;

import com.example.order_lib.dto.OrderCreatedEventDto;
import com.example.process_service.feign.OrderServiceFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedHandler {

    private final OrderServiceFeignClient orderServiceFeignClient;
    @KafkaListener(topics = "order.created")
    public void handler(OrderCreatedEventDto dto) {
        log.info("Получил эвент: {}",dto.toString());

        var orderCreatedEventDto = orderServiceFeignClient.changeStatus(dto.orderId());
        log.info("Отправил запрос на изменение статуса: {}",orderCreatedEventDto.status());
    }
}
