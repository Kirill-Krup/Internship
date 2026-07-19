package com.internship.userservice.exception;

public class CardLimitExceededException extends RuntimeException {

  public CardLimitExceededException(Long userId) {
    super(userId == null
        ? "A user cannot have more than 5 cards"
        : "User " + userId + " already has maximum of cards");
  }
}
