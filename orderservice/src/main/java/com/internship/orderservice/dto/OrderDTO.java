package com.internship.orderservice.dto;

import com.internship.orderservice.model.StatusType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderDTO {

  private Long id;

  private Long userId;

  @NotNull(message = "Status is required")
  private StatusType status;

  @Valid
  private List<OrderItemDto> orderItems;
}
