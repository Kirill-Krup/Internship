package com.internship.orderservice.dao;

import com.internship.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface OrderDao {

  Order save(Order order);

  Optional<Order> findById(Long id);

  boolean existsById(Long id);

  void delete(Order order);

  List<Order> findByIdIn(List<Long> ids);

  List<Order> findByUserId(Long userId);

  Page<Order> findAll(Specification<Order> specification, Pageable pageable);
}
