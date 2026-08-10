package com.internship.orderservice.fallback;

import com.internship.orderservice.dto.UserInfoDTO;
import com.internship.orderservice.exception.UserServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

  @Override
  public UserServiceClient create(Throwable cause) {
    return new UserServiceClient() {
      @Override
      public UserInfoDTO getUserByEmail(String email) {
        log.error("User service unavailable while fetching user by email {}", email, cause);
        throw new UserServiceUnavailableException("User service is temporarily unavailable");
      }

      @Override
      public UserInfoDTO getUserById(Long id) {
        log.error("User service unavailable while fetching user by id {}", id, cause);
        throw new UserServiceUnavailableException("User service is temporarily unavailable");
      }
    };
  }
}
