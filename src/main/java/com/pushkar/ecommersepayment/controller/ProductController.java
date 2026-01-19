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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pushkar.ecommersepayment.model.Product;
import com.pushkar.ecommersepayment.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PostMapping
  public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
    Product createdProduct = productService.createProduct(product);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
  }

  @GetMapping
  public ResponseEntity<List<Product>> getAllProducts() {
    List<Product> products = productService.getAllProducts();
    return ResponseEntity.ok(products);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable String id) {
    Product product = productService.getProductById(id);
    return ResponseEntity.ok(product);
  }

  @GetMapping("/search")
  public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
    List<Product> products = productService.searchProducts(q);
    return ResponseEntity.ok(products);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(@PathVariable String id, @Valid @RequestBody Product product) {
    Product updatedProduct = productService.updateProduct(id, product);
    return ResponseEntity.ok(updatedProduct);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable String id) {
    productService.deleteProduct(id);
    return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
  }
}
