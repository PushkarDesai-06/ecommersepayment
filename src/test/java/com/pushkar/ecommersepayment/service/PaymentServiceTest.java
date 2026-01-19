package com.pushkar.ecommersepayment.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.pushkar.ecommersepayment.dto.PaymentRequest;
import com.pushkar.ecommersepayment.model.Order;
import com.pushkar.ecommersepayment.model.Payment;
import com.pushkar.ecommersepayment.repository.OrderRepository;
import com.pushkar.ecommersepayment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Tests - Webhook Pattern")
class PaymentServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private PaymentService paymentService;

  private Order testOrder;
  private Payment testPayment;
  private PaymentRequest testRequest;

  @BeforeEach
  void setUp() {
    // Set up Razorpay credentials (mock values)
    ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "test_key_id");
    ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", "test_key_secret");

    testOrder = new Order();
    testOrder.setId("order123");
    testOrder.setUserId("user123");
    testOrder.setTotalAmount(100000.0);
    testOrder.setStatus("CREATED");

    testPayment = new Payment();
    testPayment.setId("pay123");
    testPayment.setOrderId("order123");
    testPayment.setAmount(100000.0);
    testPayment.setStatus("PENDING");
    testPayment.setRazorpayOrderId("order_razorpay123");

    testRequest = new PaymentRequest();
    testRequest.setOrderId("order123");
    testRequest.setAmount(100000.0);
  }

  @Test
  @DisplayName("Should handle payment success webhook")
  void testHandlePaymentSuccess() {
    // Arrange
    when(paymentRepository.findByRazorpayOrderId("order_razorpay123"))
        .thenReturn(Optional.of(testPayment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
    when(orderRepository.findById("order123")).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

    // Act
    paymentService.handlePaymentSuccess("pay_razorpay456", "order_razorpay123", null);

    // Assert
    verify(paymentRepository, times(1)).save(argThat(payment -> "SUCCESS".equals(payment.getStatus()) &&
        "pay_razorpay456".equals(payment.getPaymentId())));
    verify(orderRepository, times(1)).save(argThat(order -> "PAID".equals(order.getStatus())));
  }

  @Test
  @DisplayName("Should handle payment failure webhook")
  void testHandlePaymentFailure() {
    // Arrange
    when(paymentRepository.findByRazorpayOrderId("order_razorpay123"))
        .thenReturn(Optional.of(testPayment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
    when(orderRepository.findById("order123")).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

    // Act
    paymentService.handlePaymentFailure("order_razorpay123", "Insufficient funds");

    // Assert
    verify(paymentRepository, times(1)).save(argThat(payment -> "FAILED".equals(payment.getStatus())));
    verify(orderRepository, times(1)).save(argThat(order -> "FAILED".equals(order.getStatus())));
  }

  @Test
  @DisplayName("Should throw exception when payment already exists for order")
  void testCreatePayment_AlreadyExists() {
    // Arrange
    when(orderRepository.findById("order123")).thenReturn(Optional.of(testOrder));
    when(paymentRepository.findByOrderId("order123"))
        .thenReturn(Optional.of(testPayment));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> paymentService.createPayment(testRequest));
    assertTrue(exception.getMessage().contains("Payment already exists"));
  }

  @Test
  @DisplayName("Should throw exception when order not in CREATED status")
  void testCreatePayment_InvalidOrderStatus() {
    // Arrange
    testOrder.setStatus("PAID");
    when(orderRepository.findById("order123")).thenReturn(Optional.of(testOrder));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> paymentService.createPayment(testRequest));
    assertTrue(exception.getMessage().contains("not in CREATED status"));
  }

  @Test
  @DisplayName("Should get payment by order ID")
  void testGetPaymentByOrderId() {
    // Arrange
    when(paymentRepository.findByOrderId("order123"))
        .thenReturn(Optional.of(testPayment));

    // Act
    Optional<Payment> result = paymentService.getPaymentByOrderId("order123");

    // Assert
    assertTrue(result.isPresent());
    assertEquals("order123", result.get().getOrderId());
    assertEquals("PENDING", result.get().getStatus());
  }
}
