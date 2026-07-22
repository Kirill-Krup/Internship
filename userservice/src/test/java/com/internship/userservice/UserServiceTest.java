package com.internship.userservice;

import com.internship.userservice.dao.UserDao;
import com.internship.userservice.dto.UserDTO;
import com.internship.userservice.exception.EmailAlreadyExistsException;
import com.internship.userservice.exception.UserNotFoundException;
import com.internship.userservice.mapper.UserMapper;
import com.internship.userservice.model.User;
import com.internship.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserDao userDao;

  @Mock
  private UserMapper userMapper;

  @Mock
  private CacheManager cacheManager;

  @InjectMocks
  private UserServiceImpl userService;

  private User user;
  private UserDTO userDTO;
  private Timestamp pastBirthday;

  @BeforeEach
  void setUp() {
    pastBirthday = Timestamp.from(Instant.now().minus(3650, ChronoUnit.DAYS));
    user = new User();
    user.setId(1L);
    user.setName("Alice");
    user.setSurname("Smith");
    user.setEmail("test@example.com");
    user.setBirthDate(pastBirthday);
    user.setActive(true);

    userDTO = new UserDTO(1L, "Alice", "Smith", pastBirthday, "test@example.com", true, null);
  }

  @Test
  @DisplayName("Create user")
  void testCreateUser() {
    when(userDao.existsByEmail(userDTO.getEmail())).thenReturn(false);
    when(userMapper.toEntity(userDTO)).thenReturn(user);
    when(userDao.save(user)).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    UserDTO result = userService.createUser(userDTO);

    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
    verify(userDao).save(user);
  }

  @Test
  @DisplayName("Create user - email already exists")
  void testCreateUser_EmailExists() {
    when(userDao.existsByEmail(userDTO.getEmail())).thenReturn(true);
    assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(userDTO));
    verify(userDao, never()).save(any());
  }

  @Test
  @DisplayName("Get user by ID - found")
  void testGetUserById_Found() {
    when(userDao.findByIdWithCards(1L)).thenReturn(Optional.of(user));
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    UserDTO result = userService.getUserById(1L);

    assertEquals("test@example.com", result.getEmail());
  }

  @Test
  @DisplayName("Get user by ID - not found")
  void testGetUserById_NotFound() {
    when(userDao.findByIdWithCards(2L)).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> userService.getUserById(2L));
  }

  @Test
  @DisplayName("Get user by email - found")
  void testGetUserByEmail_Found() {
    when(userDao.findByEmailJpql("test@example.com")).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    UserDTO result = userService.getUserByEmail("test@example.com");

    assertEquals("test@example.com", result.getEmail());
  }

  @Test
  @DisplayName("Get user by email - not found")
  void testGetUserByEmail_NotFound() {
    when(userDao.findByEmailJpql("missing@example.com")).thenReturn(null);
    when(userDao.findByEmailNative("missing@example.com")).thenReturn(null);
    assertThrows(UserNotFoundException.class,
        () -> userService.getUserByEmail("missing@example.com"));
  }

  @Test
  @DisplayName("Get all users with filter")
  void testGetAllUsers() {
    String name = "Alice";
    String surname = "Smith";
    Pageable pageable = PageRequest.of(0, 20);

    Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
    when(userDao.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(userPage);
    when(userMapper.toDTO(user)).thenReturn(userDTO);
    Page<UserDTO> result = userService.getAllUsers(name, surname, pageable);
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getContent().size());
    assertEquals(0, result.getNumber());
    assertEquals(20, result.getSize());
    assertEquals(1, result.getTotalPages());
    assertEquals(userDTO, result.getContent().get(0));
    verify(userDao).findAll(any(Specification.class), eq(pageable));
    verify(userMapper).toDTO(user);
  }
  @Test
  @DisplayName("Get users by IDs")
  void testGetUsersByIds() {
    when(userDao.findByIds(List.of(1L))).thenReturn(List.of(user));
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    List<UserDTO> result = userService.getUsersByIds(List.of(1L));

    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Update user - success")
  void testUpdateUser_Success() {
    UserDTO updatedDTO = new UserDTO(1L, "AliceUpdated", "Smith", pastBirthday,
        "new@example.com", true, null);
    when(userDao.findById(1L)).thenReturn(Optional.of(user));
    when(userDao.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
    when(userDao.save(any(User.class))).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(updatedDTO);

    UserDTO result = userService.updateUser(1L, updatedDTO);

    assertEquals("AliceUpdated", result.getName());
    verify(userMapper).updateEntityFromDto(updatedDTO, user);
    verify(userDao).save(user);
  }

  @Test
  @DisplayName("Update user - not found")
  void testUpdateUser_NotFound() {
    when(userDao.findById(99L)).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> userService.updateUser(99L, userDTO));
  }

  @Test
  @DisplayName("Activate user")
  void testActivateUser() {
    user.setActive(true);
    when(userDao.existsById(1L)).thenReturn(true);
    when(userDao.findByIdWithCards(1L)).thenReturn(Optional.of(user));
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    UserDTO result = userService.activateUser(1L);

    assertTrue(result.getActive());
    verify(userDao).activateById(1L);
  }

  @Test
  @DisplayName("Deactivate user")
  void testDeactivateUser() {
    UserDTO inactive = new UserDTO(1L, "Alice", "Smith", pastBirthday, "test@example.com", false,
        null);
    user.setActive(false);
    when(userDao.existsById(1L)).thenReturn(true);
    when(userDao.findByIdWithCards(1L)).thenReturn(Optional.of(user));
    when(userMapper.toDTO(user)).thenReturn(inactive);

    UserDTO result = userService.deactivateUser(1L);

    assertFalse(result.getActive());
    verify(userDao).deactivateById(1L);
  }

  @Test
  @DisplayName("Delete user - success")
  void testDeleteUser_Success() {
    when(userDao.findByIdWithCards(1L)).thenReturn(Optional.of(user));
    when(userMapper.toDTO(user)).thenReturn(userDTO);

    UserDTO result = userService.deleteUser(1L);

    assertEquals(1L, result.getId());
    verify(userDao).deleteById(1L);
  }

  @Test
  @DisplayName("Delete user - not found")
  void testDeleteUser_NotFound() {
    when(userDao.findByIdWithCards(99L)).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));
    verify(userDao, never()).deleteById(any());
  }
}
