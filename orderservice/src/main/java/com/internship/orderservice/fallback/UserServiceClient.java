package com.internship.orderservice.fallback;

import com.internship.orderservice.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "userservice",
    url = "${user.service.url}",
    fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {

  @GetMapping("/api/v1/users/email/{email}")
  UserInfoDTO getUserByEmail(@PathVariable("email") String email);

  @GetMapping("/api/v1/users/{id}")
  UserInfoDTO getUserById(@PathVariable("id") Long id);
}
