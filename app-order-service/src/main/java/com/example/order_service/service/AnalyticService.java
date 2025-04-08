package com.example.order_service.service;

import com.example.order_lib.dto.AnalyticDto;
import com.example.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticService {

    private final OrderRepository orderRepository;

    @Transactional
    public AnalyticDto getAnalytic() {
        return new AnalyticDto(
                orderRepository.countOrdersToday(),
                orderRepository.findAverageProcessingTimeInSeconds(),
                orderRepository.countOrdersByStatus(),
                orderRepository.countOrdersByCustomer()
        );
    }
}
