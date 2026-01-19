package com.pushkar.ecommersepayment.service;

import java.time.Instant;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pushkar.ecommersepayment.dto.CreateOrderRequest;
import com.pushkar.ecommersepayment.dto.OrderResponse;
import com.pushkar.ecommersepayment.model.CartItem;
import com.pushkar.ecommersepayment.model.Order;
import com.pushkar.ecommersepayment.model.OrderItem;
import com.pushkar.ecommersepayment.model.Payment;
import com.pushkar.ecommersepayment.model.Product;
import com.pushkar.ecommersepayment.repository.CartRepository;
import com.pushkar.ecommersepayment.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests - Business Logic")
class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private ProductService productService;

  @Mock
  private PaymentService paymentService;

  @InjectMocks
  private OrderService orderService;

  private Product testProduct;
  private CartItem testCartItem;
  private Order testOrder;
  private CreateOrderRequest testRequest;

  @BeforeEach
  void setUp() {
    testProduct = new Product();
    testProduct.setId("prod123");
    testProduct.setName("Laptop");
    testProduct.setPrice(50000.0);
    testProduct.setStock(10);

    testCartItem = new CartItem();
    testCartItem.setUserId("user123");
    testCartItem.setProductId("prod123");
    testCartItem.setQuantity(2);

    testOrder = new Order();
    testOrder.setId("order123");
    testOrder.setUserId("user123");
    testOrder.setTotalAmount(100000.0);
    testOrder.setStatus("CREATED");
    testOrder.setCreatedAt(Instant.now());

    testRequest = new CreateOrderRequest();
    testRequest.setUserId("user123");
  }

  @Test
  @DisplayName("Should create order from cart - Business Logic Test")
  void testCreateOrder_Success() {
    // Arrange
    when(cartRepository.findByUserId("user123"))
        .thenReturn(Arrays.asList(testCartItem));
    when(productService.getProductById("prod123")).thenReturn(testProduct);
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
    doNothing().when(productService).updateStock(anyString(), anyInt());
    doNothing().when(cartRepository).deleteByUserId(anyString());

    // Act
    Order result = orderService.createOrder(testRequest);

    // Assert
    assertNotNull(result);
    assertEquals("user123", result.getUserId());
    assertEquals("CREATED", result.getStatus());
    assertEquals(100000.0, result.getTotalAmount());

    // Verify business logic
    verify(productService, times(1)).updateStock("prod123", -2); // Stock deducted
    verify(cartRepository, times(1)).deleteByUserId("user123"); // Cart cleared
    verify(orderRepository, times(1)).save(any(Order.class)); // Order saved
  }

  @Test
  @DisplayName("Should throw exception when cart is empty")
  void testCreateOrder_EmptyCart() {
    // Arrange
    when(cartRepository.findByUserId("user123")).thenReturn(Arrays.asList());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> orderService.createOrder(testRequest));
    assertTrue(exception.getMessage().contains("Cart is empty"));
    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when insufficient stock")
  void testCreateOrder_InsufficientStock() {
    // Arrange
    testProduct.setStock(1); // Not enough for 2 items
    when(cartRepository.findByUserId("user123"))
        .thenReturn(Arrays.asList(testCartItem));
    when(productService.getProductById("prod123")).thenReturn(testProduct);

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> orderService.createOrder(testRequest));
    assertTrue(exception.getMessage().contains("Insufficient stock"));
  }

  @Test
  @DisplayName("Should get order with payment details")
  void testGetOrderById_WithPayment() {
    // Arrange
    Payment payment = new Payment();
    payment.setId("pay123");
    payment.setOrderId("order123");
    payment.setStatus("SUCCESS");
    payment.setAmount(100000.0);

    OrderItem orderItem = new OrderItem();
    orderItem.setProductId("prod123");
    orderItem.setQuantity(2);
    orderItem.setPrice(50000.0);
    testOrder.setItems(Arrays.asList(orderItem));

    when(orderRepository.findById("order123")).thenReturn(Optional.of(testOrder));
    when(paymentService.getPaymentByOrderId("order123")).thenReturn(Optional.of(payment));

    // Act
    OrderResponse result = orderService.getOrderById("order123");

    // Assert
    assertNotNull(result);
    assertEquals("order123", result.getId());
    assertEquals("user123", result.getUserId());
    assertNotNull(result.getPayment());
    assertEquals("SUCCESS", result.getPayment().getStatus());
    assertEquals(1, result.getItems().size());
  }

  @Test
  @DisplayName("Should update order status")
  void testUpdateOrderStatus() {
    // Arrange
    when(orderRepository.findById("order123")).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

    // Act
    orderService.updateOrderStatus("order123", "PAID");

    // Assert
    verify(orderRepository, times(1)).save(argThat(order -> "PAID".equals(order.getStatus())));
  }

  @Test
  @DisplayName("Should cancel order and restore stock")
  void testCancelOrder() {
    // Arrange
    OrderItem orderItem = new OrderItem();
    orderItem.setProductId("prod123");
    orderItem.setQuantity(2);
    orderItem.setPrice(50000.0);
    testOrder.setItems(Arrays.asList(orderItem));

    when(orderRepository.findById("order123")).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
    doNothing().when(productService).updateStock(anyString(), anyInt());

    // Act
    orderService.cancelOrder("order123");

    // Assert
    verify(productService, times(1)).updateStock("prod123", 2); // Stock restored
    verify(orderRepository, times(1)).save(argThat(order -> "CANCELLED".equals(order.getStatus())));
  }

  @Test
  @DisplayName("Should not cancel order if status is not CREATED")
  void testCancelOrder_InvalidStatus() {
    // Arrange
    testOrder.setStatus("PAID");
    when(orderRepository.findById("order123")).thenReturn(Optional.of(testOrder));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> orderService.cancelOrder("order123"));
    assertTrue(exception.getMessage().contains("Cannot cancel order"));
  }

  @Test
  @DisplayName("Should get orders by user ID - Database Relationship Test")
  void testGetOrdersByUserId() {
    // Arrange
    Order order2 = new Order();
    order2.setId("order456");
    order2.setUserId("user123");
    order2.setTotalAmount(50000.0);
    order2.setStatus("PAID");

    when(orderRepository.findByUserId("user123"))
        .thenReturn(Arrays.asList(testOrder, order2));

    // Act
    List<Order> results = orderService.getOrdersByUserId("user123");

    // Assert
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(o -> "user123".equals(o.getUserId())));
  }
}
