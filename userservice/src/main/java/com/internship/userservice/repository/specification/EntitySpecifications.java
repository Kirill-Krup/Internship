package com.internship.userservice.repository.specification;

import com.internship.userservice.model.CardInfo;
import com.internship.userservice.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class EntitySpecifications {

  private EntitySpecifications() {}

  public static Specification<User> userHasName(String name) {
    return (root, query, cb) -> {
      if (!StringUtils.hasText(name)) {
        return cb.conjunction();
      }
      return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    };
  }

  public static Specification<User> userHasSurname(String surname) {
    return (root, query, cb) -> {
      if (!StringUtils.hasText(surname)) {
        return cb.conjunction();
      }
      return cb.like(cb.lower(root.get("surname")), "%" + surname.toLowerCase() + "%");
    };
  }

  public static Specification<CardInfo> cardHasUserName(String name) {
    return (root, query, cb) -> {
      if (!StringUtils.hasText(name)) {
        return cb.conjunction();
      }
      Join<CardInfo, User> user = root.join("user", JoinType.INNER);
      if (query != null) {
        query.distinct(true);
      }
      return cb.like(cb.lower(user.get("name")), "%" + name.toLowerCase() + "%");
    };
  }

  public static Specification<CardInfo> cardHasUserSurname(String surname) {
    return (root, query, cb) -> {
      if (!StringUtils.hasText(surname)) {
        return cb.conjunction();
      }
      Join<CardInfo, User> user = root.join("user", JoinType.INNER);
      if (query != null) {
        query.distinct(true);
      }
      return cb.like(cb.lower(user.get("surname")), "%" + surname.toLowerCase() + "%");
    };
  }
}
