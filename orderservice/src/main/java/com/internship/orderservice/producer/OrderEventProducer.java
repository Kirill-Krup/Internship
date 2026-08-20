package com.internship.orderservice.producer;

import com.internship.orderservice.dto.OrderResponseDTO;
import com.internship.orderservice.dto.event.CreateOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

  public static final String CREATE_ORDER_TOPIC = "CREATE_ORDER";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void sendCreateOrderEvent(OrderResponseDTO orderDTO, BigDecimal totalPrice) {
    CreateOrderEvent event = new CreateOrderEvent();
    event.setOrderId(orderDTO.getId());
    event.setAmount(totalPrice);
    event.setUserId(orderDTO.getUserId());
    kafkaTemplate.send(CREATE_ORDER_TOPIC, event);
    log.debug("Sent CREATE_ORDER event: {}", event);
  }
}
