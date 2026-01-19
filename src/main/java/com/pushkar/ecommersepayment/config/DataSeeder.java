package com.pushkar.ecommersepayment.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.pushkar.ecommersepayment.model.Product;
import com.pushkar.ecommersepayment.model.User;
import com.pushkar.ecommersepayment.repository.ProductRepository;
import com.pushkar.ecommersepayment.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  @Override
  public void run(String... args) {
    log.info("Starting data seeding...");

    // Check if data already exists
    if (productRepository.count() > 0) {
      log.info("Data already exists, skipping seeding");
      return;
    }

    // Seed users
    User user1 = new User(null, "john_doe", "john@example.com", "customer");
    User user2 = new User(null, "jane_smith", "jane@example.com", "customer");
    userRepository.saveAll(List.of(user1, user2));
    log.info("Seeded {} users", 2);

    // Seed products
    Product laptop = new Product(null, "Laptop", "Gaming Laptop", 50000.0, 10);
    Product mouse = new Product(null, "Mouse", "Wireless Mouse", 1000.0, 50);
    Product keyboard = new Product(null, "Keyboard", "Mechanical Keyboard", 3000.0, 30);
    Product monitor = new Product(null, "Monitor", "27-inch 4K Monitor", 25000.0, 15);
    Product headphones = new Product(null, "Headphones", "Noise-Cancelling Headphones", 5000.0, 40);

    productRepository.saveAll(List.of(laptop, mouse, keyboard, monitor, headphones));
    log.info("Seeded {} products", 5);

    log.info("Data seeding completed successfully!");
  }
}
