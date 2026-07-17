package com.internship.userservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String surname;

  @Column(name = "birth_date")
  private Timestamp birthDate;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private Boolean active = true;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JsonManagedReference
  private List<CardInfo> cards = new ArrayList<>();
}
