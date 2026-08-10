package com.internship.orderservice.dto;

import com.internship.orderservice.model.StatusType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

  private Long id;
  private Long userId;
  private StatusType status;
  private BigDecimal totalPrice;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<OrderItemDto> orderItems;
  private UserInfoDTO user;
}
