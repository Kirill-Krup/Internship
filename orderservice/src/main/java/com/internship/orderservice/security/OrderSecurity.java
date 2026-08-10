package com.internship.orderservice.security;

import com.internship.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

  private final OrderRepository orderRepository;

  public boolean isOrderOwner(Long orderId, Long userId) {
    return orderRepository.existsByIdAndUserId(orderId, userId);
  }
}
