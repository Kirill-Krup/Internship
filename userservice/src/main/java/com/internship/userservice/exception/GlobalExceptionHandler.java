package com.internship.userservice.exception;

import com.internship.userservice.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
    ErrorResponse error = new ErrorResponse("USER NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(CardInfoNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCardInfoNotFoundException(
      CardInfoNotFoundException e) {
    ErrorResponse error = new ErrorResponse("CARD INFO NOT FOUND", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
      EmailAlreadyExistsException e) {
    ErrorResponse error = new ErrorResponse("EMAIL ALREADY EXISTS", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(CardLimitExceededException.class)
  public ResponseEntity<ErrorResponse> handleCardLimitExceededException(
      CardLimitExceededException e) {
    ErrorResponse error = new ErrorResponse("CARD_LIMIT_EXCEEDED", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", errorMessage);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(UserNotDeletedException.class)
  public ResponseEntity<ErrorResponse> handleUserNotDeletedException(UserNotDeletedException e) {
    ErrorResponse error = new ErrorResponse("USER_NOT_DELETED", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
    log.error("Unexpected error", e);
    ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
