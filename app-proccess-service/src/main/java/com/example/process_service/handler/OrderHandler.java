package com.example.process_service.handler;

import com.example.order_lib.dto.OrderDto;
import com.example.process_service.feign.OrderServiceFeignClient;
import com.example.process_service.model.OrderStatus;
import com.example.process_service.model.Status;
import com.example.process_service.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderHandler {

    private final OrderServiceFeignClient orderServiceFeignClient;
    private final OrderStatusRepository orderStatusRepository;
    private final RedisTemplate<String, OrderDto> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "order:";

    @KafkaListener(topics = "order.created")
    public void handler(ConsumerRecord<Long, OrderDto> record) {
        var dto = record.value();
        var traceHeader = record.headers().lastHeader("traceId");
        var traceId = new String(traceHeader.value());
        MDC.put("traceId", traceId);
        log.info(">> Получил информацию о заказе: {}", dto.orderId());
        log.debug("traceId заказа {} : {}", dto.orderId(), traceId);

        var orderStatus = OrderStatus.builder()
                .orderId(dto.orderId())
                .status(Status.PROCESSED)
                .build();
        orderStatusRepository.save(orderStatus);

        clearAllOrders();
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + dto.orderId(), dto);

        log.info(">> Заказ {} обработан", dto.orderId());

        var response = orderServiceFeignClient.changeStatus(dto.orderId());
        if (response.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
            log.info(">> Статус заказа {} успешно изменен", dto.orderId());
        } else {
            log.error(">> Ошибка при обновлении статуса заказа {}, код ошибки: {}", dto.orderId(), response.getStatusCode());
        }
        MDC.remove("traceId");
    }

    private void clearAllOrders() {
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        log.debug(">> Все предыдущие заказы удалены из Redis");
    }
}
