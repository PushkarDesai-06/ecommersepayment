package com.pushkar.ecommersepayment.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pushkar.ecommersepayment.dto.CreateOrderRequest;
import com.pushkar.ecommersepayment.dto.OrderResponse;
import com.pushkar.ecommersepayment.model.Order;
import com.pushkar.ecommersepayment.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    Order order = orderService.createOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(order);
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
    OrderResponse order = orderService.getOrderById(orderId);
    return ResponseEntity.ok(order);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable String userId) {
    List<Order> orders = orderService.getOrdersByUserId(userId);
    return ResponseEntity.ok(orders);
  }

  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable String orderId) {
    orderService.cancelOrder(orderId);
    return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
  }
}
