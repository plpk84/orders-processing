package com.example.order_lib.dto;

import java.util.Map;

public record AnalyticDto(
        Long orderCount,
        Double avrProcessTime,
        Map<String, Long> orderCountGroupByStatus,
        Map<String, Long> orderCountGroupByCustomers
) {
}
