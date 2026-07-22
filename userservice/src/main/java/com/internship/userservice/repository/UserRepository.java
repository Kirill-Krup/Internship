package com.internship.userservice.repository;

import com.internship.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, Long id);

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.cards WHERE u.email = :email")
  User findUserByEmailJPQL(@Param("email") String email);

  @Query(value = """
      SELECT DISTINCT u.* FROM users u
      LEFT JOIN payment_cards c ON u.id = c.user_id
      WHERE u.email = :email
      """, nativeQuery = true)
  User findUserByEmailNative(@Param("email") String email);

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.cards WHERE u.id = :id")
  Optional<User> findUserById(@Param("id") Long id);

  @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.cards WHERE u.id IN :ids")
  List<User> findUsersByIdIn(@Param("ids") List<Long> ids);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE User u SET u.active = true WHERE u.id = :id")
  void activateById(@Param("id") Long id);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE User u SET u.active = false WHERE u.id = :id")
  void deactivateById(@Param("id") Long id);
}
