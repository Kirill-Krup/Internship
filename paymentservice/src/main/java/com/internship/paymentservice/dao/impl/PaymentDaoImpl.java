package com.internship.paymentservice.dao.impl;

import com.internship.paymentservice.dao.PaymentDao;
import com.internship.paymentservice.model.Payment;
import com.internship.paymentservice.model.enums.PaymentStatus;
import com.internship.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentDaoImpl implements PaymentDao {

  private final PaymentRepository paymentRepository;

  @Override
  public Payment save(Payment payment) {
    return paymentRepository.save(payment);
  }

  @Override
  public Optional<Payment> findById(String id) {
    return paymentRepository.findById(id);
  }

  @Override
  public boolean existsById(String id) {
    return paymentRepository.existsById(id);
  }

  @Override
  public void deleteById(String id) {
    paymentRepository.deleteById(id);
  }

  @Override
  public List<Payment> findByOrderId(Long orderId) {
    return paymentRepository.findByOrderId(orderId);
  }

  @Override
  public List<Payment> findByUserId(Long userId) {
    return paymentRepository.findByUserId(userId);
  }

  @Override
  public List<Payment> findByStatusIn(List<PaymentStatus> statuses) {
    return paymentRepository.findByStatusIn(statuses);
  }

  @Override
  public Double getTotalSumForPeriod(Instant from, Instant to) {
    return paymentRepository.getTotalSumForPeriod(from, to);
  }

  @Override
  public Double getTotalSumForPeriodByUserId(Long userId, Instant from, Instant to) {
    return paymentRepository.getTotalSumForPeriodByUserId(userId, from, to);
  }
}
