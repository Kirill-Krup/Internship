package com.internship.paymentservice.model;

import com.internship.paymentservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payments")
public class Payment {

  @Id
  private String id;

  @Indexed
  @Field("order_id")
  private Long orderId;

  @Indexed
  @Field("user_id")
  private Long userId;

  @Indexed
  private PaymentStatus status;

  @Indexed
  private Instant timestamp;

  @Field("payment_amount")
  private BigDecimal paymentAmount;
}
