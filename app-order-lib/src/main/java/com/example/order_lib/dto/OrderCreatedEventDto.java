package com.example.order_lib.dto;

import java.util.List;

public record OrderCreatedEventDto(
        Long orderId,
        String customerFullName,
        List<OrderItemDto> items,
        String deliveryAddress,
        String status
) {
}
