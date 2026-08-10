package com.internship.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CreateUserRequest {

  @NotBlank(message = "Name is required")
  private final String name;

  @NotBlank(message = "Surname is required")
  private final String surname;

  @NotNull(message = "Birthday is required")
  @Past(message = "Birthday must be in the past")
  private final Timestamp birthDate;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private final String email;
}