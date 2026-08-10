package com.internship.userservice.controller;

import com.internship.userservice.dto.UserDTO;
import com.internship.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/create")
  public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
    UserDTO newUser = userService.createUser(userDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<UserDTO>> getAllUsers(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String surname, @PageableDefault(size = 20, sort = "id",
                  direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(userService.getAllUsers(name, surname, pageable));
  }

  @GetMapping("/ids")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<UserDTO>> getUsersByIds(@RequestParam List<Long> ids) {
    return ResponseEntity.ok(userService.getUsersByIds(ids));
  }

  @GetMapping("/email/{email}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userService.getUserByEmail(email));
  }

  @GetMapping("/get-id-by-email/{email}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Long> getUserIdByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userService.getUserIdByEmail(email));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
  public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
  public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
      @Valid @RequestBody UserDTO userDto) {
    return ResponseEntity.ok(userService.updateUser(id, userDto));
  }

  @PutMapping("/{id}/activate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> activateUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.activateUser(id));
  }

  @PutMapping("/{id}/deactivate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> deactivateUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.deactivateUser(id));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> deleteUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.deleteUser(id));
  }
}
