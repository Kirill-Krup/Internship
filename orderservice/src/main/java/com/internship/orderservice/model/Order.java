package com.internship.orderservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
@ToString
@SQLDelete(sql = "UPDATE orders SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Order extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StatusType status;

  @Column(name = "total_price", nullable = false)
  private BigDecimal totalPrice = BigDecimal.ZERO;

  @Column(nullable = false)
  private boolean deleted = false;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<OrderItem> orderItems;
}
