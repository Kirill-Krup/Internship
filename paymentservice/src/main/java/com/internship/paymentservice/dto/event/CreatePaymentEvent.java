package com.internship.paymentservice.dto.event;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentEvent {
  private String paymentId;
  private Long orderId;
  private String status;
}
