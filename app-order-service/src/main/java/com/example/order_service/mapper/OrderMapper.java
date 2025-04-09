package com.example.order_service.mapper;

import com.example.order_lib.dto.OrderCreatedEventDto;
import com.example.order_lib.dto.OrderRequestDto;
import com.example.order_service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR, uses = OrderItemMapper.class)
public interface OrderMapper {
    @Mapping(target = "status", expression = "java(com.example.order_service.model.enums.Status.IN_PROGRESS)")
    Order toEntity(OrderRequestDto dto);

    OrderCreatedEventDto toDto(Order entity);
}
