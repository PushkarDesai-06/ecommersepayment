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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pushkar.ecommersepayment.dto.AddToCartRequest;
import com.pushkar.ecommersepayment.dto.CartItemResponse;
import com.pushkar.ecommersepayment.model.CartItem;
import com.pushkar.ecommersepayment.model.Product;
import com.pushkar.ecommersepayment.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Service Tests")
class CartServiceTest {

  @Mock
  private CartRepository cartRepository;

  @Mock
  private ProductService productService;

  @InjectMocks
  private CartService cartService;

  private Product testProduct;
  private CartItem testCartItem;
  private AddToCartRequest testRequest;

  @BeforeEach
  void setUp() {
    testProduct = new Product();
    testProduct.setId("prod123");
    testProduct.setName("Laptop");
    testProduct.setPrice(50000.0);
    testProduct.setStock(10);

    testCartItem = new CartItem();
    testCartItem.setId("cart123");
    testCartItem.setUserId("user123");
    testCartItem.setProductId("prod123");
    testCartItem.setQuantity(2);

    testRequest = new AddToCartRequest();
    testRequest.setUserId("user123");
    testRequest.setProductId("prod123");
    testRequest.setQuantity(2);
  }

  @Test
  @DisplayName("Should add new item to cart")
  void testAddToCart_NewItem() {
    // Arrange
    when(productService.getProductById("prod123")).thenReturn(testProduct);
    when(cartRepository.findByUserIdAndProductId("user123", "prod123"))
        .thenReturn(Optional.empty());
    when(cartRepository.save(any(CartItem.class))).thenReturn(testCartItem);

    // Act
    CartItem result = cartService.addToCart(testRequest);

    // Assert
    assertNotNull(result);
    assertEquals("user123", result.getUserId());
    assertEquals("prod123", result.getProductId());
    assertEquals(2, result.getQuantity());
    verify(cartRepository, times(1)).save(any(CartItem.class));
  }

  @Test
  @DisplayName("Should update quantity when item already in cart")
  void testAddToCart_ExistingItem() {
    // Arrange
    when(productService.getProductById("prod123")).thenReturn(testProduct);
    when(cartRepository.findByUserIdAndProductId("user123", "prod123"))
        .thenReturn(Optional.of(testCartItem));
    when(cartRepository.save(any(CartItem.class))).thenReturn(testCartItem);

    // Act
    CartItem result = cartService.addToCart(testRequest);

    // Assert
    verify(cartRepository, times(1)).save(argThat(item -> item.getQuantity() == 4 // 2 existing + 2 new
    ));
  }

  @Test
  @DisplayName("Should throw exception when insufficient stock")
  void testAddToCart_InsufficientStock() {
    // Arrange
    testProduct.setStock(1);
    testRequest.setQuantity(5);
    when(productService.getProductById("prod123")).thenReturn(testProduct);

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> cartService.addToCart(testRequest));
    assertTrue(exception.getMessage().contains("Insufficient stock"));
  }

  @Test
  @DisplayName("Should get cart items with product details")
  void testGetCartItems() {
    // Arrange
    when(cartRepository.findByUserId("user123"))
        .thenReturn(Arrays.asList(testCartItem));
    when(productService.getProductById("prod123")).thenReturn(testProduct);

    // Act
    List<CartItemResponse> results = cartService.getCartItems("user123");

    // Assert
    assertEquals(1, results.size());
    CartItemResponse response = results.get(0);
    assertEquals("user123", response.getUserId());
    assertEquals("prod123", response.getProductId());
    assertEquals(2, response.getQuantity());
    assertNotNull(response.getProduct());
    assertEquals("Laptop", response.getProduct().getName());
  }

  @Test
  @DisplayName("Should clear cart successfully")
  void testClearCart() {
    // Arrange
    doNothing().when(cartRepository).deleteByUserId("user123");

    // Act
    cartService.clearCart("user123");

    // Assert
    verify(cartRepository, times(1)).deleteByUserId("user123");
  }
}
