package com.internship.userservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "payment_cards")
public class CardInfo extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String number;

  @Column(nullable = false)
  private String holder;

  @Column(name = "expiration_date")
  private Timestamp expirationDate;

  @Column(nullable = false)
  private Boolean active = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonBackReference
  private User user;
}
