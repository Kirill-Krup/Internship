package com.internship.userservice.repository;

import com.internship.userservice.model.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Long>,
    JpaSpecificationExecutor<CardInfo> {

  List<CardInfo> findByIdIn(List<Long> ids);

  List<CardInfo> findByUserId(Long userId);

  long countByUserId(Long userId);

  @Query("SELECT c FROM CardInfo c WHERE c.user.id = :userId")
  List<CardInfo> findCardsByUserIdJPQL(@Param("userId") Long userId);

  @Query(value = "SELECT * FROM payment_cards WHERE user_id = :userId", nativeQuery = true)
  List<CardInfo> findCardsByUserIdNative(@Param("userId") Long userId);
}
