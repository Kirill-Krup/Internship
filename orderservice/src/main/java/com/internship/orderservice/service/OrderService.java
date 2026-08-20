package com.internship.orderservice.service;

import com.internship.orderservice.dto.OrderDTO;
import com.internship.orderservice.dto.OrderResponseDTO;
import com.internship.orderservice.model.StatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

  OrderResponseDTO createOrder(OrderDTO order, String email);

  OrderResponseDTO getOrderById(Long orderId);

  Page<OrderResponseDTO> getOrders(
      Pageable pageable,
      LocalDateTime createdFrom,
      LocalDateTime createdTo,
      List<StatusType> statuses);

  List<OrderResponseDTO> getOrdersByUserId(Long userId);

  OrderResponseDTO updateOrder(Long id, OrderDTO order);

  void deleteOrder(Long orderId);

  void updateOrderStatusFromPayment(Long orderId, StatusType status);
}
