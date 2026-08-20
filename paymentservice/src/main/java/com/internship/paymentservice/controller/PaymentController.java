package com.internship.paymentservice.controller;

import com.internship.paymentservice.dto.PaymentCreateDTO;
import com.internship.paymentservice.dto.PaymentDTO;
import com.internship.paymentservice.model.enums.PaymentStatus;
import com.internship.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/createPayment")
  @PreAuthorize("hasRole('ADMIN') or #paymentDTO.userId == authentication.principal.userId")
  public ResponseEntity<PaymentDTO> createPayment(@RequestBody @Valid PaymentCreateDTO paymentDTO) {
    return ResponseEntity.ok(paymentService.createPayment(paymentDTO));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @paymentSecurity.isPaymentOwner(#id, authentication.principal.userId)")
  public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable String id) {
    return ResponseEntity.ok(paymentService.getPaymentById(id));
  }

  @GetMapping("/order/{orderId}")
  @PreAuthorize("hasRole('ADMIN') or @paymentSecurity.isOrderPaymentOwner(#orderId, authentication.principal.userId)")
  public ResponseEntity<List<PaymentDTO>> getPaymentByOrderId(@PathVariable Long orderId) {
    return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
  public ResponseEntity<List<PaymentDTO>> getPaymentByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(paymentService.getPaymentsByUserId(userId));
  }

  @GetMapping("/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(
      @RequestParam List<PaymentStatus> statuses) {
    return ResponseEntity.ok(paymentService.findPaymentsByStatus(statuses));
  }

  @GetMapping("/summary/period/user/{userId}")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
  public ResponseEntity<Double> getTotalSumForPeriodByUserId(
      @PathVariable Long userId,
      @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
    return ResponseEntity.ok(paymentService.getTotalSumForPeriodByUserId(userId, from, to));
  }

  @GetMapping("/summary/period/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Double> getTotalSumForPeriodForAllUsers(
      @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
    return ResponseEntity.ok(paymentService.getTotalSumForPeriodForAllUsers(from, to));
  }

  @DeleteMapping("/deletePayment/{id}")
  @PreAuthorize("hasRole('ADMIN') or @paymentSecurity.isPaymentOwner(#id, authentication.principal.userId)")
  public ResponseEntity<Void> deletePayment(@PathVariable String id) {
    paymentService.deletePayment(id);
    return ResponseEntity.ok().build();
  }
}
