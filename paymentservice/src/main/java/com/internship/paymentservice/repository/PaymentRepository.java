package com.internship.paymentservice.repository;

import com.internship.paymentservice.model.Payment;
import com.internship.paymentservice.model.enums.PaymentStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

  List<Payment> findByOrderId(Long orderId);

  List<Payment> findByUserId(Long userId);

  List<Payment> findByStatusIn(List<PaymentStatus> statuses);

  boolean existsByIdAndUserId(String id, Long userId);

  boolean existsByOrderIdAndUserId(Long orderId, Long userId);

  @Aggregation(pipeline = {
      "{$match: {timestamp: {$gte: ?0, $lte: ?1}}}",
      "{$group: {_id: null, total: {$sum: {$toDouble: '$payment_amount'}}}}"
  })
  Double getTotalSumForPeriod(Instant from, Instant to);

  @Aggregation(pipeline = {
      "{$match: {user_id: ?0, timestamp: {$gte: ?1, $lte: ?2}}}",
      "{$group: {_id: null, total: {$sum: {$toDouble: '$payment_amount'}}}}"
  })
  Double getTotalSumForPeriodByUserId(Long userId, Instant from, Instant to);
}
