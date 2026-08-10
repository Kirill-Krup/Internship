package com.internship.orderservice.controller;

import com.internship.orderservice.dto.OrderDTO;
import com.internship.orderservice.dto.OrderResponseDTO;
import com.internship.orderservice.model.StatusType;
import com.internship.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or #email == authentication.principal.login")
  public ResponseEntity<OrderResponseDTO> createOrder(
      @Valid @RequestBody OrderDTO orderDTO,
      @RequestParam String email) {
    OrderResponseDTO created = orderService.createOrder(orderDTO, email);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#id, authentication.principal.userId)")
  public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.getOrderById(id));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<OrderResponseDTO>> getOrders(
      Pageable pageable,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @RequestParam(required = false) List<StatusType> statuses) {
    return ResponseEntity.ok(orderService.getOrders(pageable, from, to, statuses));
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
  public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#id, authentication.principal.userId)")
  public ResponseEntity<OrderResponseDTO> updateOrder(
      @PathVariable Long id,
      @Valid @RequestBody OrderDTO orderDTO) {
    return ResponseEntity.ok(orderService.updateOrder(id, orderDTO));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#id, authentication.principal.userId)")
  public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
    orderService.deleteOrder(id);
    return ResponseEntity.noContent().build();
  }
}
