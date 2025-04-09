package com.example.order_lib.dto;

import java.util.List;

public record OrderRequestDto(
        String customerFullName,
        List<OrderItemDto>items,
        String deliveryAddress
) {
}
