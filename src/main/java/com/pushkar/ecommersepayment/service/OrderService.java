package com.pushkar.ecommersepayment.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pushkar.ecommersepayment.dto.CreateOrderRequest;
import com.pushkar.ecommersepayment.dto.OrderResponse;
import com.pushkar.ecommersepayment.model.CartItem;
import com.pushkar.ecommersepayment.model.Order;
import com.pushkar.ecommersepayment.model.OrderItem;
import com.pushkar.ecommersepayment.model.Product;
import com.pushkar.ecommersepayment.repository.CartRepository;
import com.pushkar.ecommersepayment.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final ProductService productService;
  private final PaymentService paymentService;

  @Transactional
  public Order createOrder(CreateOrderRequest request) {
    log.info("Creating order for user: {}", request.getUserId());

    // Get cart items
    List<CartItem> cartItems = cartRepository.findByUserId(request.getUserId());
    if (cartItems.isEmpty()) {
      throw new RuntimeException("Cart is empty");
    }

    // Validate stock and create order items
    List<OrderItem> orderItems = new ArrayList<>();
    double totalAmount = 0.0;

    for (CartItem cartItem : cartItems) {
      Product product = productService.getProductById(cartItem.getProductId());

      if (product.getStock() < cartItem.getQuantity()) {
        throw new RuntimeException("Insufficient stock for product: " + product.getName()
            + ". Available: " + product.getStock());
      }

      OrderItem orderItem = new OrderItem();
      orderItem.setProductId(product.getId());
      orderItem.setQuantity(cartItem.getQuantity());
      orderItem.setPrice(product.getPrice());
      orderItems.add(orderItem);

      totalAmount += product.getPrice() * cartItem.getQuantity();

      // Update stock
      productService.updateStock(product.getId(), -cartItem.getQuantity());
    }

    // Create order
    Order order = new Order();
    order.setUserId(request.getUserId());
    order.setTotalAmount(totalAmount);
    order.setStatus("CREATED");
    order.setItems(orderItems);
    order.setCreatedAt(Instant.now());

    Order savedOrder = orderRepository.save(order);

    // Clear cart
    cartRepository.deleteByUserId(request.getUserId());

    log.info("Order created successfully: {}", savedOrder.getId());
    return savedOrder;
  }

  public OrderResponse getOrderById(String orderId) {
    log.info("Fetching order: {}", orderId);
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

    OrderResponse response = new OrderResponse();
    response.setId(order.getId());
    response.setUserId(order.getUserId());
    response.setTotalAmount(order.getTotalAmount());
    response.setStatus(order.getStatus());
    response.setItems(order.getItems());
    response.setCreatedAt(order.getCreatedAt());

    // Get payment if exists
    paymentService.getPaymentByOrderId(orderId).ifPresent(response::setPayment);

    return response;
  }

  public List<Order> getOrdersByUserId(String userId) {
    log.info("Fetching orders for user: {}", userId);
    return orderRepository.findByUserId(userId);
  }

  @Transactional
  public void updateOrderStatus(String orderId, String status) {
    log.info("Updating order {} status to: {}", orderId, status);
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    order.setStatus(status);
    orderRepository.save(order);
  }

  @Transactional
  public void cancelOrder(String orderId) {
    log.info("Cancelling order: {}", orderId);
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

    if (!"CREATED".equals(order.getStatus())) {
      throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
    }

    // Restore stock
    for (OrderItem item : order.getItems()) {
      productService.updateStock(item.getProductId(), item.getQuantity());
    }

    order.setStatus("CANCELLED");
    orderRepository.save(order);
  }
}
