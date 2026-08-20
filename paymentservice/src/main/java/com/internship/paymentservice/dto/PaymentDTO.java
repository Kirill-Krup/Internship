package com.internship.paymentservice.dto;

import com.internship.paymentservice.model.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@RequiredArgsConstructor
public class PaymentDTO {
  private final String id;

  @NotNull(message = "OrderId cannot be null")
  @Positive(message = "OrderId must be positive")
  private final Long orderId;

  @NotNull(message = "UserId cannot be null")
  @Positive(message = "UserId must be positive")
  private final Long userId;

  private final PaymentStatus status;

  private final Instant timestamp;

  @NotNull(message = "Payment amount is required")
  @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
  private final BigDecimal paymentAmount;
}
