package com.internship.authservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "credentials")
public class UserCredentials {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Column(nullable = false, unique = true)
  private String login;

  @Column(name = "password_hash", nullable = false)
  private String hashedPassword;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;
}
