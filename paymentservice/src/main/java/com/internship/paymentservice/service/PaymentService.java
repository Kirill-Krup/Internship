package com.internship.paymentservice.service;

import com.internship.paymentservice.dto.PaymentCreateDTO;
import com.internship.paymentservice.dto.PaymentDTO;
import com.internship.paymentservice.model.enums.PaymentStatus;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;

public interface PaymentService {

  PaymentDTO createPayment(@Valid PaymentCreateDTO paymentDTO);

  void deletePayment(String id);

  PaymentDTO getPaymentById(String id);

  List<PaymentDTO> getPaymentsByOrderId(Long orderId);

  List<PaymentDTO> getPaymentsByUserId(Long userId);

  List<PaymentDTO> findPaymentsByStatus(List<PaymentStatus> statuses);

  Double getTotalSumForPeriodByUserId(Long userId, Instant from, Instant to);

  Double getTotalSumForPeriodForAllUsers(Instant from, Instant to);
}
