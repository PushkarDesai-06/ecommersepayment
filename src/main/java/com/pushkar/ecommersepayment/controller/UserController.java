package com.pushkar.ecommersepayment.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pushkar.ecommersepayment.model.User;
import com.pushkar.ecommersepayment.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<User> createUser(@RequestBody User user) {
    User createdUser = userService.createUser(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }

  @GetMapping
  public ResponseEntity<List<User>> getAllUsers() {
    List<User> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable String id) {
    User user = userService.getUserById(id);
    return ResponseEntity.ok(user);
  }

  @PutMapping("/{id}")
  public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
    User updatedUser = userService.updateUser(id, user);
    return ResponseEntity.ok(updatedUser);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String id) {
    userService.deleteUser(id);
    return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
  }
}
