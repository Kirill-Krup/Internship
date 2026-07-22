package com.internship.userservice.dao.impl;

import com.internship.userservice.dao.UserDao;
import com.internship.userservice.model.User;
import com.internship.userservice.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

  private final UserRepository userRepository;

  public UserDaoImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public User save(User user) {
    return userRepository.save(user);
  }

  @Override
  public Optional<User> findById(Long id) {
    return userRepository.findById(id);
  }

  @Override
  public Optional<User> findByIdWithCards(Long id) {
    return userRepository.findUserById(id);
  }

  @Override
  public User findByEmailJpql(String email) {
    return userRepository.findUserByEmailJPQL(email);
  }

  @Override
  public User findByEmailNative(String email) {
    return userRepository.findUserByEmailNative(email);
  }

  @Override
  public List<User> findByIds(List<Long> ids) {
    return userRepository.findUsersByIdIn(ids);
  }

  @Override
  public Page<User> findAll(Specification<User> specification, Pageable pageable) {
    return userRepository.findAll(specification, pageable);
  }

  @Override
  public boolean existsById(Long id) {
    return userRepository.existsById(id);
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Override
  public boolean existsByEmailAndIdNot(String email, Long id) {
    return userRepository.existsByEmailAndIdNot(email, id);
  }

  @Override
  public void deleteById(Long id) {
    userRepository.deleteById(id);
  }

  @Override
  public void activateById(Long id) {
    userRepository.activateById(id);
  }

  @Override
  public void deactivateById(Long id) {
    userRepository.deactivateById(id);
  }
}
