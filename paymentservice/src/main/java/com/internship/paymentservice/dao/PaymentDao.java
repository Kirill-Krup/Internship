package com.internship.paymentservice.dao;

import com.internship.paymentservice.model.Payment;
import com.internship.paymentservice.model.enums.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentDao {

  Payment save(Payment payment);

  Optional<Payment> findById(String id);

  boolean existsById(String id);

  void deleteById(String id);

  List<Payment> findByOrderId(Long orderId);

  List<Payment> findByUserId(Long userId);

  List<Payment> findByStatusIn(List<PaymentStatus> statuses);

  Double getTotalSumForPeriod(Instant from, Instant to);

  Double getTotalSumForPeriodByUserId(Long userId, Instant from, Instant to);
}
