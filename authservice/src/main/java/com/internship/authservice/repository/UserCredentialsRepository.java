package com.internship.authservice.repository;

import com.internship.authservice.model.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
    boolean existsByLogin(String login);
    Optional<UserCredentials> findByLogin(String login);
}
