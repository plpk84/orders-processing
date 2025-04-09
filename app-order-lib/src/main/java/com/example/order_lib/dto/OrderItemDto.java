package com.example.order_lib.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        Long orderItemId,
        String productId,
        int quantity,
        BigDecimal price
) {
}
