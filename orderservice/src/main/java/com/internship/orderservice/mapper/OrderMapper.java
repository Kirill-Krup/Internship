package com.internship.orderservice.mapper;

import com.internship.orderservice.dto.OrderDTO;
import com.internship.orderservice.dto.OrderResponseDTO;
import com.internship.orderservice.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

  @Mapping(target = "user", ignore = true)
  OrderResponseDTO toResponseDTO(Order order);

  List<OrderResponseDTO> toResponseDTO(List<Order> orders);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "totalPrice", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "orderItems", ignore = true)
  Order toEntity(OrderDTO orderDTO);
}
