package com.internship.userservice.dao;

import com.internship.userservice.model.CardInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface CardInfoDao {

  CardInfo save(CardInfo cardInfo);

  Optional<CardInfo> findById(Long id);

  List<CardInfo> findByIds(List<Long> ids);

  List<CardInfo> findByUserId(Long userId);

  List<CardInfo> findByUserIdJpql(Long userId);

  List<CardInfo> findByUserIdNative(Long userId);

  long countByUserId(Long userId);

  Page<CardInfo> findAll(Specification<CardInfo> specification, Pageable pageable);

  boolean existsById(Long id);

  void deleteById(Long id);

  void activateById(Long id);

  void deactivateById(Long id);

  boolean existsByIdAndUserId(Long cardId, Long userId);
}
