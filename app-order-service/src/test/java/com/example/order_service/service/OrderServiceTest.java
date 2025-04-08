package com.example.order_service.service;

import com.example.order_lib.dto.OrderDto;
import com.example.order_lib.dto.OrderItemDto;
import com.example.order_lib.dto.OrderRequestDto;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.enums.Status;
import com.example.order_service.repository.OrderRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private KafkaTemplate<Long, OrderDto> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<ProducerRecord<Long, OrderDto>> producerRecordCaptor;

    private Order order;
    private OrderDto orderDto;
    private OrderRequestDto orderRequestDto;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderId(1L);
        order.setStatus(Status.IN_PROGRESS);
        order.setCustomerFullName("fullName");
        order.setDeliveryAddress("address");
        order.setCreatedAt(LocalDateTime.now());

        var item = new OrderItem(
                1L, "product", 1, BigDecimal.ONE, order
        );
        order.setItems(List.of(item));

        var orderItemDto = new OrderItemDto(1L, "product", 1, BigDecimal.ONE);

        orderDto = new OrderDto(1L, "fullName", List.of(orderItemDto), "address", "IN_PROGRESS");

        orderRequestDto = new OrderRequestDto("fullName", List.of(orderItemDto), "address");
    }

    @Test
    void testGetOrderById_ShouldReturnOrderDto() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        // when
        OrderDto result = orderService.getOrderById(1L);

        //then
        assertNotNull(result);
        assertEquals(orderDto, result);
        verify(orderRepository).findById(1L);
        verify(orderMapper).toDto(order);
    }

    @Test
    void testGetOrderById_ShouldThrowException_WhenOrderNotExists() {
        //when
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        //then
        assertThrows(RuntimeException.class, () -> orderService.getOrderById(1L));
        verify(orderRepository).findById(1L);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void testSaveOrder_ShouldSaveOrderAndSendKafkaMessage() {
        //given
        when(orderMapper.toEntity(orderRequestDto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        //when
        OrderDto result = orderService.saveOrder(orderRequestDto);

        //then
        assertNotNull(result);
        assertEquals(orderDto, result);
        verify(orderMapper).toEntity(orderRequestDto);
        verify(orderRepository).save(order);
        verify(orderMapper).toDto(order);
        verify(kafkaTemplate).send(producerRecordCaptor.capture());
        ProducerRecord<Long, OrderDto> capturedRecord = producerRecordCaptor.getValue();
        assertEquals("order.created", capturedRecord.topic());
        assertEquals(orderDto, capturedRecord.value());
        assertNotNull(capturedRecord.headers().lastHeader("traceId"));
        assertNull(MDC.get("traceId"));
    }

    @Test
    void changeStatus_ShouldThrowException_WhenOrderNotExists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.changeStatus(1L));
        verify(orderRepository).findById(1L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
    }
}