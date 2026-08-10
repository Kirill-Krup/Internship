package com.internship.orderservice.dao.impl;

import com.internship.orderservice.dao.OrderDao;
import com.internship.orderservice.model.Order;
import com.internship.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderDaoImpl implements OrderDao {

  private final OrderRepository orderRepository;

  @Override
  public Order save(Order order) {
    return orderRepository.save(order);
  }

  @Override
  public Optional<Order> findById(Long id) {
    return orderRepository.findById(id);
  }

  @Override
  public boolean existsById(Long id) {
    return orderRepository.existsById(id);
  }

  @Override
  public void delete(Order order) {
    orderRepository.delete(order);
  }

  @Override
  public List<Order> findByIdIn(List<Long> ids) {
    return orderRepository.findByIdIn(ids);
  }

  @Override
  public List<Order> findByUserId(Long userId) {
    return orderRepository.findByUserId(userId);
  }

  @Override
  public Page<Order> findAll(Specification<Order> specification, Pageable pageable) {
    return orderRepository.findAll(specification, pageable);
  }
}
