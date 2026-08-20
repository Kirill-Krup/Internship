package com.internship.paymentservice.producer;

import com.internship.paymentservice.dto.PaymentDTO;
import com.internship.paymentservice.dto.event.CreatePaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

  public static final String CREATE_PAYMENT_TOPIC = "CREATE_PAYMENT";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void sendCreatePaymentEvent(PaymentDTO paymentDTO) {
    CreatePaymentEvent event = CreatePaymentEvent.builder()
        .paymentId(paymentDTO.getId())
        .orderId(paymentDTO.getOrderId())
        .status(paymentDTO.getStatus().toString())
        .build();
    kafkaTemplate.send(CREATE_PAYMENT_TOPIC, event);
    log.debug("Sent CREATE_PAYMENT event: {}", event);
  }
}
