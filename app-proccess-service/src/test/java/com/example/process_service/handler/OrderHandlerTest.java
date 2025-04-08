package com.example.process_service.handler;

import com.example.order_lib.dto.OrderDto;
import com.example.order_lib.dto.OrderItemDto;
import com.example.process_service.feign.OrderServiceFeignClient;
import com.example.process_service.model.OrderStatus;
import com.example.process_service.model.Status;
import com.example.process_service.repository.OrderStatusRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderHandlerTest {

    @Mock
    private OrderServiceFeignClient orderServiceFeignClient;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private RedisTemplate<String, OrderDto> redisTemplate;
    @Mock
    private ValueOperations<String, OrderDto> valueOperations;

    @InjectMocks
    private OrderHandler orderHandler;
    private final Long orderId = 1L;
    private OrderDto orderDto;
    private ConsumerRecord<Long, OrderDto> record;
    private static final String REDIS_KEY_PREFIX = "order:";

    @BeforeEach
    void setUp() {
        var traceId = "test-trace-id";
        orderDto = new OrderDto(
                orderId,
                "FullName",
                List.of(
                        new OrderItemDto(
                                1L,
                                "productId",
                                10,
                                BigDecimal.ONE
                        )
                ),
                "adress",
                "IN_PROGRESS"
        );
        record = new ConsumerRecord<>("order.created", 0, 0, orderId, orderDto);
        record.headers().add("traceId", traceId.getBytes());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testHandler_shouldProcessOrderCorrectly() {
        //given
        when(redisTemplate.keys(REDIS_KEY_PREFIX + "*"))
                .thenReturn(Collections.emptySet());
        when(orderServiceFeignClient.changeStatus(orderId))
                .thenReturn(ResponseEntity.ok().build());

        // when
        orderHandler.handler(record);

        //then
        ArgumentCaptor<OrderStatus> orderStatusCaptor = ArgumentCaptor.forClass(OrderStatus.class);
        verify(orderStatusRepository).save(orderStatusCaptor.capture());
        OrderStatus savedOrderStatus = orderStatusCaptor.getValue();
        assertEquals(orderId, savedOrderStatus.getOrderId());
        assertEquals(Status.PROCESSED, savedOrderStatus.getStatus());

        verify(redisTemplate.opsForValue()).set(REDIS_KEY_PREFIX + orderId, orderDto);

        verify(orderServiceFeignClient).changeStatus(orderId);

        assertNull(MDC.get("traceId"));
    }
}
