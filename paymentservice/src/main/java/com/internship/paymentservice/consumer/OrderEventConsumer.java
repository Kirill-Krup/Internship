package com.internship.paymentservice.consumer;

import com.internship.paymentservice.dto.PaymentCreateDTO;
import com.internship.paymentservice.dto.event.CreateOrderEvent;
import com.internship.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

  private final PaymentService paymentService;

  @KafkaListener(topics = "CREATE_ORDER", groupId = "payment-service-group")
  public void handleCreateOrder(CreateOrderEvent event) {
    log.debug("New event CREATE_ORDER: {}", event);
    PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO(
        event.getOrderId(),
        event.getUserId(),
        event.getAmount()
    );
    paymentService.createPayment(paymentCreateDTO);
  }
}
