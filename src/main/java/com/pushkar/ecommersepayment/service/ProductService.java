package com.pushkar.ecommersepayment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pushkar.ecommersepayment.model.Product;
import com.pushkar.ecommersepayment.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

  private final ProductRepository productRepository;

  public Product createProduct(Product product) {
    log.info("Creating product: {}", product.getName());
    return productRepository.save(product);
  }

  public List<Product> getAllProducts() {
    log.info("Fetching all products");
    return productRepository.findAll();
  }

  public Product getProductById(String id) {
    log.info("Fetching product by id: {}", id);
    return productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
  }

  public List<Product> searchProducts(String query) {
    log.info("Searching products with query: {}", query);
    return productRepository.findByNameContainingIgnoreCase(query);
  }

  public Product updateProduct(String id, Product product) {
    log.info("Updating product: {}", id);
    Product existingProduct = getProductById(id);
    existingProduct.setName(product.getName());
    existingProduct.setDescription(product.getDescription());
    existingProduct.setPrice(product.getPrice());
    existingProduct.setStock(product.getStock());
    return productRepository.save(existingProduct);
  }

  public void deleteProduct(String id) {
    log.info("Deleting product: {}", id);
    productRepository.deleteById(id);
  }

  public void updateStock(String productId, Integer quantity) {
    log.info("Updating stock for product: {} by quantity: {}", productId, quantity);
    Product product = getProductById(productId);
    product.setStock(product.getStock() + quantity);
    productRepository.save(product);
  }
}
