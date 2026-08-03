package com.internship.userservice.service;

import com.internship.userservice.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

  UserDTO createUser(UserDTO userDTO);

  UserDTO getUserById(Long id);

  UserDTO getUserByEmail(String email);

  Long getUserIdByEmail(String email);

  List<UserDTO> getUsersByIds(List<Long> ids);

  Page<UserDTO> getAllUsers(String name, String surname, Pageable pageable);

  UserDTO updateUser(Long id, UserDTO updated);

  UserDTO activateUser(Long id);

  UserDTO deactivateUser(Long id);

  UserDTO deleteUser(Long id);
}
