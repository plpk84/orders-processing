package com.example.order_service.mapper;

import com.example.order_lib.dto.OrderItemDto;
import com.example.order_service.model.OrderItem;
import org.mapstruct.Mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface OrderItemMapper {
    OrderItem toEntity(OrderItemDto dto);

    OrderItemDto toDto(OrderItem entity);
}
