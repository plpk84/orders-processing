package com.example.order_service.repository;

import com.example.order_service.model.Order;
import com.example.order_service.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COUNT(o) FROM Order o WHERE CAST(o.createdAt AS localdate) = LOCAL DATE")
    long countOrdersToday();

    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, o.createdAt, o.updatedAt)) FROM Order o WHERE o.updatedAt IS NOT NULL")
    Double findAverageProcessingTimeInSeconds();

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderCountsByStatus();

    default Map<String, Long> countOrdersByStatus() {
        return getOrderCountsByStatus().stream()
                .collect(Collectors.toMap(
                        obj -> ((Status) obj[0]).name(), // Преобразуем Status в String
                        obj -> (Long) obj[1]
                ));
    }

    @Query("SELECT o.customerFullName, COUNT(o) FROM Order o GROUP BY o.customerFullName")
    List<Object[]> getOrderCountsByCustomer();

    default Map<String, Long> countOrdersByCustomer() {
        return getOrderCountsByCustomer().stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));
    }
}
