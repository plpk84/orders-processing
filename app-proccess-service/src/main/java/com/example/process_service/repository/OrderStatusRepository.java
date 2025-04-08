package com.example.process_service.repository;

import com.example.process_service.model.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderStatusRepository extends MongoRepository<OrderStatus, Long> {
}
