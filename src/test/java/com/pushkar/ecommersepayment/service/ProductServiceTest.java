package com.pushkar.ecommersepayment.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pushkar.ecommersepayment.model.Product;
import com.pushkar.ecommersepayment.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private ProductService productService;

  private Product testProduct;

  @BeforeEach
  void setUp() {
    testProduct = new Product();
    testProduct.setId("prod123");
    testProduct.setName("Laptop");
    testProduct.setDescription("Gaming Laptop");
    testProduct.setPrice(50000.0);
    testProduct.setStock(10);
  }

  @Test
  @DisplayName("Should create product successfully")
  void testCreateProduct() {
    // Arrange
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    // Act
    Product created = productService.createProduct(testProduct);

    // Assert
    assertNotNull(created);
    assertEquals("Laptop", created.getName());
    assertEquals(50000.0, created.getPrice());
    assertEquals(10, created.getStock());
    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  @DisplayName("Should get all products")
  void testGetAllProducts() {
    // Arrange
    Product product2 = new Product();
    product2.setId("prod456");
    product2.setName("Mouse");
    product2.setPrice(1000.0);
    product2.setStock(50);

    List<Product> products = Arrays.asList(testProduct, product2);
    when(productRepository.findAll()).thenReturn(products);

    // Act
    List<Product> result = productService.getAllProducts();

    // Assert
    assertEquals(2, result.size());
    verify(productRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should get product by ID")
  void testGetProductById() {
    // Arrange
    when(productRepository.findById("prod123")).thenReturn(Optional.of(testProduct));

    // Act
    Product found = productService.getProductById("prod123");

    // Assert
    assertNotNull(found);
    assertEquals("prod123", found.getId());
    assertEquals("Laptop", found.getName());
  }

  @Test
  @DisplayName("Should throw exception when product not found")
  void testGetProductByIdNotFound() {
    // Arrange
    when(productRepository.findById("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> productService.getProductById("invalid"));
    assertTrue(exception.getMessage().contains("Product not found"));
  }

  @Test
  @DisplayName("Should update stock correctly")
  void testUpdateStock() {
    // Arrange
    when(productRepository.findById("prod123")).thenReturn(Optional.of(testProduct));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    // Act
    productService.updateStock("prod123", -2);

    // Assert
    verify(productRepository, times(1)).save(argThat(product -> product.getStock() == 8));
  }

  @Test
  @DisplayName("Should search products by name")
  void testSearchProducts() {
    // Arrange
    when(productRepository.findByNameContainingIgnoreCase("laptop"))
        .thenReturn(Arrays.asList(testProduct));

    // Act
    List<Product> results = productService.searchProducts("laptop");

    // Assert
    assertEquals(1, results.size());
    assertEquals("Laptop", results.get(0).getName());
  }
}
