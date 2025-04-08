package com.example.process_service.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "order_status")
@Data
@Builder
public class OrderStatus {
    @Id
    Long orderId;
    Status status;
}
