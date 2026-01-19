package com.pushkar.ecommersepayment.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pushkar.ecommersepayment.dto.PaymentRequest;
import com.pushkar.ecommersepayment.model.Order;
import com.pushkar.ecommersepayment.model.Payment;
import com.pushkar.ecommersepayment.repository.OrderRepository;
import com.pushkar.ecommersepayment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;

  @Value("${payment.mock.enabled:true}")
  private boolean mockEnabled;

  @Value("${payment.mock.success.delay:2000}")
  private long mockDelay;

  @Value("${payment.mock.failure.probability:0.1}")
  private double failureProbability;

  @Transactional
  public Payment createPayment(PaymentRequest request) {
    log.info("Creating mock payment for order: {}", request.getOrderId());

    // Validate order exists and is in CREATED status
    Order order = orderRepository.findById(request.getOrderId())
        .orElseThrow(() -> new RuntimeException("Order not found with id: " + request.getOrderId()));

    if (!"CREATED".equals(order.getStatus())) {
      throw new RuntimeException("Order is not in CREATED status. Current status: " + order.getStatus());
    }

    // Check if payment already exists
    Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());
    if (existingPayment.isPresent()) {
      throw new RuntimeException("Payment already exists for this order");
    }

    // Create mock payment order
    String mockOrderId = "mock_order_" + UUID.randomUUID().toString();
    String mockPaymentId = "mock_pay_" + UUID.randomUUID().toString();

    // Save payment record
    Payment payment = new Payment();
    payment.setOrderId(request.getOrderId());
    payment.setAmount(request.getAmount());
    payment.setStatus("PENDING");
    payment.setRazorpayOrderId(mockOrderId);
    payment.setPaymentId(mockPaymentId);
    payment.setCreatedAt(Instant.now());

    Payment savedPayment = paymentRepository.save(payment);
    log.info("Mock payment created successfully: {}", savedPayment.getId());

    // Simulate async payment processing
    if (mockEnabled) {
      simulatePaymentProcessing(mockPaymentId, mockOrderId);
    }

    return savedPayment;
  }

  private void simulatePaymentProcessing(String mockPaymentId, String mockOrderId) {
    new Thread(() -> {
      try {
        // Simulate payment gateway delay
        Thread.sleep(mockDelay);

        // Random success/failure based on configured probability
        boolean success = Math.random() > failureProbability;

        if (success) {
          log.info("Mock payment succeeded: {}", mockPaymentId);
          handlePaymentSuccess(mockPaymentId, mockOrderId, "mock_signature");
        } else {
          log.info("Mock payment failed: {}", mockPaymentId);
          handlePaymentFailure(mockOrderId, "Mock payment failure - insufficient funds");
        }
      } catch (InterruptedException e) {
        log.error("Payment simulation interrupted", e);
        Thread.currentThread().interrupt();
      }
    }).start();
  }

  @Transactional
  public void handlePaymentSuccess(String paymentId, String mockOrderId, String signature) {
    log.info("Processing payment success - Payment ID: {}, Order ID: {}", paymentId, mockOrderId);

    Payment payment = paymentRepository.findByRazorpayOrderId(mockOrderId)
        .orElseThrow(() -> new RuntimeException("Payment not found for order: " + mockOrderId));

    payment.setPaymentId(paymentId);
    payment.setStatus("SUCCESS");
    paymentRepository.save(payment);

    // Update order status
    Order order = orderRepository.findById(payment.getOrderId())
        .orElseThrow(() -> new RuntimeException("Order not found: " + payment.getOrderId()));
    order.setStatus("PAID");
    orderRepository.save(order);

    log.info("Payment processed successfully for order: {}", order.getId());
  }

  @Transactional
  public void handlePaymentFailure(String mockOrderId, String reason) {
    log.info("Processing payment failure - Order ID: {}, Reason: {}", mockOrderId, reason);

    Payment payment = paymentRepository.findByRazorpayOrderId(mockOrderId)
        .orElseThrow(() -> new RuntimeException("Payment not found for order: " + mockOrderId));

    payment.setStatus("FAILED");
    paymentRepository.save(payment);

    // Update order status
    Order order = orderRepository.findById(payment.getOrderId())
        .orElseThrow(() -> new RuntimeException("Order not found: " + payment.getOrderId()));
    order.setStatus("FAILED");
    orderRepository.save(order);

    log.info("Payment failure processed for order: {}", order.getId());
  }

  public Optional<Payment> getPaymentByOrderId(String orderId) {
    return paymentRepository.findByOrderId(orderId);
  }

  public Payment getPaymentById(String paymentId) {
    return paymentRepository.findById(paymentId)
        .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
  }
}
