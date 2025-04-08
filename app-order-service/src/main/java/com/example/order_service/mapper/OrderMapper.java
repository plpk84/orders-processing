package com.example.order_service.mapper;

import com.example.order_lib.dto.OrderDto;
import com.example.order_lib.dto.OrderRequestDto;
import com.example.order_service.model.Order;
import org.mapstruct.Mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR, uses = OrderItemMapper.class)
public interface OrderMapper {
    Order toEntity(OrderRequestDto dto);

    OrderDto toDto(Order entity);
}
