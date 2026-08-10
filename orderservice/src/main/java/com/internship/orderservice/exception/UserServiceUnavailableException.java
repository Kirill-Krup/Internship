package com.internship.orderservice.exception;

public class UserServiceUnavailableException extends RuntimeException {

  public UserServiceUnavailableException(String message) {
    super(message);
  }
}
