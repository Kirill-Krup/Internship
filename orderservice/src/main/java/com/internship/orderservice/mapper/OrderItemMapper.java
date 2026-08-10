package com.internship.orderservice.mapper;

import com.internship.orderservice.dto.OrderItemDto;
import com.internship.orderservice.model.OrderItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface OrderItemMapper {

  OrderItemDto toDto(OrderItem orderItem);

  OrderItem toEntity(OrderItemDto orderItemDto);

  List<OrderItemDto> toDto(List<OrderItem> orderItemList);
}
