package com.internship.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CardInfoDTO {

  private final Long id;

  @NotBlank(message = "Number is required")
  private final String number;

  @NotBlank(message = "Holder is required")
  private final String holder;

  private final Timestamp expirationDate;

  private final Boolean active;

  private final Long userId;
}
