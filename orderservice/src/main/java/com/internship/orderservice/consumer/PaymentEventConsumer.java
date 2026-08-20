package com.internship.orderservice.consumer;

import com.internship.orderservice.dto.event.CreatePaymentEvent;
import com.internship.orderservice.model.StatusType;
import com.internship.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

  private final OrderService orderService;

  @KafkaListener(topics = "CREATE_PAYMENT", groupId = "order-service-group")
  public void handleCreatePayment(CreatePaymentEvent event) {
    log.debug("Received CREATE_PAYMENT event: {}", event);
    StatusType status = "SUCCESS".equalsIgnoreCase(event.getStatus())
        ? StatusType.PAID
        : StatusType.PAYMENT_FAILED;
    orderService.updateOrderStatusFromPayment(event.getOrderId(), status);
  }
}
