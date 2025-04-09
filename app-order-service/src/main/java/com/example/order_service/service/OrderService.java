package com.example.order_service.service;

import com.example.order_lib.dto.OrderCreatedEventDto;
import com.example.order_lib.dto.OrderRequestDto;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.example.order_service.model.enums.Status.PROCESSED;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final KafkaTemplate<Long, OrderCreatedEventDto> kafkaTemplate;

    public OrderCreatedEventDto getOrderById(Long id) {
        return mapper.toDto(orderRepository.findById(id).orElseThrow());
    }

    @Transactional
    public OrderCreatedEventDto saveOrder(OrderRequestDto orderRequestDto) {
        var order = mapper.toEntity(orderRequestDto);
        final var traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        order = orderRepository.save(order);
        log.info("Заказ создан : {}", order.getOrderId());
        MDC.remove("traceId");
        final var orderDto = mapper.toDto(order);
        CompletableFuture<SendResult<Long, OrderCreatedEventDto>> future = kafkaTemplate.send(
                new ProducerRecord<Long, OrderCreatedEventDto>(
                        "order.created",
                        null,
                        orderDto.orderId(),
                        orderDto,
                        List.of(new RecordHeader("traceId", traceId.getBytes()))
                )
        );
        SendResult<Long, OrderCreatedEventDto> responseDtoSendResult = future.join();
        log.info("Сообщение отправлено по топику: {}",responseDtoSendResult.getRecordMetadata().topic());
        log.info("Партиция: {}",responseDtoSendResult.getRecordMetadata().partition());
        return orderDto;
    }

    public OrderCreatedEventDto changeStatus(Long id) {
        var order = orderRepository.findById(id).orElseThrow();
        order.setStatus(PROCESSED);
        orderRepository.save(order);
        return mapper.toDto(order);
    }
}
