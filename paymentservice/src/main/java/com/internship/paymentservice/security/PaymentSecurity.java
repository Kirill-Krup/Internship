package com.internship.paymentservice.security;

import com.internship.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("paymentSecurity")
@RequiredArgsConstructor
public class PaymentSecurity {

  private final PaymentRepository paymentRepository;

  public boolean isPaymentOwner(String paymentId, Long userId) {
    return paymentRepository.existsByIdAndUserId(paymentId, userId);
  }

  public boolean isOrderPaymentOwner(Long orderId, Long userId) {
    return paymentRepository.existsByOrderIdAndUserId(orderId, userId);
  }
}
