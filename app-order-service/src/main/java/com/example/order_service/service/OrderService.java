package com.example.order_service.service;

import com.example.order_lib.dto.OrderDto;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.example.order_service.model.enums.Status.PROCESSED;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final KafkaTemplate<Long, OrderDto> kafkaTemplate;

    @Transactional
    public OrderDto getOrderById(Long id) {
        return orderMapper.toDto(orderRepository.findById(id).orElseThrow());
    }

    @Transactional
    public OrderDto saveOrder(OrderRequestDto orderRequestDto) {
        var orderMapperEntity = orderMapper.toEntity(orderRequestDto);
        orderMapperEntity.getItems().forEach(item -> item.setOrder(orderMapperEntity));

        var order = orderRepository.save(orderMapperEntity);
        log.info(">> Заказ создан id: {}", order.getOrderId());

        final var traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        log.debug(">> traceId заказа {} : {}", order.getOrderId(), traceId);

        final var orderDto = orderMapper.toDto(order);
        kafkaTemplate.send(
                new ProducerRecord<Long, OrderDto>(
                        "order.created",
                        null,
                        orderDto.orderId(),
                        orderDto,
                        List.of(new RecordHeader("traceId", traceId.getBytes()))
                )
        );
        MDC.remove("traceId");
        return orderDto;
    }

    @Transactional
    public OrderDto changeStatus(Long id) {
        var order = orderRepository.findById(id).orElseThrow();
        order.setStatus(PROCESSED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.saveAndFlush(order);
        log.info(">> Статус заказа {} изменен", order.getOrderId());
        return orderMapper.toDto(order);
    }
}
