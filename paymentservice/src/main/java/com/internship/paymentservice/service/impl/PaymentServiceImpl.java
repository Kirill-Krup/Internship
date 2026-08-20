package com.internship.paymentservice.service.impl;

import com.internship.paymentservice.client.RandomNumberClient;
import com.internship.paymentservice.dao.PaymentDao;
import com.internship.paymentservice.dto.PaymentCreateDTO;
import com.internship.paymentservice.dto.PaymentDTO;
import com.internship.paymentservice.exception.NumberValidationException;
import com.internship.paymentservice.exception.PaymentNotFoundException;
import com.internship.paymentservice.mapper.PaymentMapper;
import com.internship.paymentservice.model.Payment;
import com.internship.paymentservice.model.enums.PaymentStatus;
import com.internship.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

  private final PaymentDao paymentDao;
  private final PaymentMapper paymentMapper;
  private final RandomNumberClient randomNumberClient;

  @Override
  public PaymentDTO createPayment(PaymentCreateDTO paymentDTO) {
    int randomNumber = getRandomNumber();
    log.debug("Api sent randomNumber: {}", randomNumber);
    PaymentStatus status = (randomNumber % 2 == 0) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    Payment payment = paymentMapper.toEntity(paymentDTO);
    payment.setStatus(status);
    payment.setTimestamp(Instant.now());
    PaymentDTO savedPayment = paymentMapper.toDto(paymentDao.save(payment));
    return savedPayment;
  }

  @Override
  public void deletePayment(String id) {
    if (!paymentDao.existsById(id)) {
      throw new PaymentNotFoundException(id);
    }
    paymentDao.deleteById(id);
  }

  @Override
  public PaymentDTO getPaymentById(String id) {
    Payment payment = paymentDao.findById(id)
        .orElseThrow(() -> new PaymentNotFoundException(id));
    return paymentMapper.toDto(payment);
  }

  @Override
  public List<PaymentDTO> getPaymentsByOrderId(Long orderId) {
    return paymentDao.findByOrderId(orderId)
        .stream()
        .map(paymentMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<PaymentDTO> getPaymentsByUserId(Long userId) {
    return paymentDao.findByUserId(userId)
        .stream()
        .map(paymentMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<PaymentDTO> findPaymentsByStatus(List<PaymentStatus> statuses) {
    return paymentDao.findByStatusIn(statuses)
        .stream()
        .map(paymentMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public Double getTotalSumForPeriodByUserId(Long userId, Instant from, Instant to) {
    return paymentDao.getTotalSumForPeriodByUserId(userId, from, to);
  }

  @Override
  public Double getTotalSumForPeriodForAllUsers(Instant from, Instant to) {
    return paymentDao.getTotalSumForPeriod(from, to);
  }

  private int getRandomNumber() {
    int randomNumber = randomNumberClient.getRandomNumber();
    if (randomNumber < 0) {
      throw new NumberValidationException();
    }
    return randomNumber;
  }
}
