package com.internship.userservice.service;

import com.internship.userservice.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {

  UserDTO createUser(UserDTO userDTO);

  Optional<UserDTO> getUserById(Long id);

  UserDTO getUserByEmail(String email);

  List<UserDTO> getUsersByIds(List<Long> ids);

  Page<UserDTO> getAllUsers(String name, String surname, Pageable pageable);

  UserDTO updateUser(Long id, UserDTO updated);

  UserDTO activateUser(Long id);

  UserDTO deactivateUser(Long id);

  UserDTO deleteUser(Long id);
}
