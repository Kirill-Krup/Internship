package com.internship.orderservice.repository.specification;

import com.internship.orderservice.model.Order;
import com.internship.orderservice.model.StatusType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class OrderSpecifications {

  private OrderSpecifications() {
  }

  public static Specification<Order> withFilters(
      LocalDateTime createdFrom,
      LocalDateTime createdTo,
      List<StatusType> statuses) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (createdFrom != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
      }
      if (createdTo != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdTo));
      }
      if (statuses != null && !statuses.isEmpty()) {
        predicates.add(root.get("status").in(statuses));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
