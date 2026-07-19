package com.internship.userservice.dao;

import com.internship.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface UserDao {

  User save(User user);

  Optional<User> findById(Long id);

  Optional<User> findByIdWithCards(Long id);

  User findByEmailJpql(String email);

  User findByEmailNative(String email);

  List<User> findByIds(List<Long> ids);

  Page<User> findAll(Specification<User> spec, Pageable pageable);

  boolean existsById(Long id);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, Long id);

  void deleteById(Long id);
}
