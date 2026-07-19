package com.internship.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CreateCardInfoDTO {

  @NotBlank(message = "Number is required")
  private final String number;

  @NotNull(message = "User id is required")
  private final Long userId;

  @NotBlank(message = "Holder is required")
  private final String holder;

  private final Timestamp expirationDate;
}
