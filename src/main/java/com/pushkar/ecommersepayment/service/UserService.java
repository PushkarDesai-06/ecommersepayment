package com.pushkar.ecommersepayment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pushkar.ecommersepayment.model.User;
import com.pushkar.ecommersepayment.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;

  public User createUser(User user) {
    log.info("Creating user: {}", user.getUsername());
    return userRepository.save(user);
  }

  public List<User> getAllUsers() {
    log.info("Fetching all users");
    return userRepository.findAll();
  }

  public User getUserById(String id) {
    log.info("Fetching user by id: {}", id);
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
  }

  public User updateUser(String id, User user) {
    log.info("Updating user: {}", id);
    User existingUser = getUserById(id);
    existingUser.setUsername(user.getUsername());
    existingUser.setEmail(user.getEmail());
    existingUser.setRole(user.getRole());
    return userRepository.save(existingUser);
  }

  public void deleteUser(String id) {
    log.info("Deleting user: {}", id);
    userRepository.deleteById(id);
  }
}
