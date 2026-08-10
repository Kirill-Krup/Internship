package com.internship.orderservice.repository;

import com.internship.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

  List<Order> findByIdIn(List<Long> ids);

  List<Order> findByUserId(Long userId);

  boolean existsByIdAndUserId(Long id, Long userId);
}
