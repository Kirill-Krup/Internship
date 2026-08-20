package com.internship.paymentservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderEvent {
  private Long orderId;
  private Long userId;
  private BigDecimal amount;
}
