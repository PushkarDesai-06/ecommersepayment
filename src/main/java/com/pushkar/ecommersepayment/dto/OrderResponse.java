package com.pushkar.ecommersepayment.dto;

import java.time.Instant;
import java.util.List;

import com.pushkar.ecommersepayment.model.OrderItem;
import com.pushkar.ecommersepayment.model.Payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

  private String id;

  private String userId;

  private Double totalAmount;

  private String status;

  private List<OrderItem> items;

  private Payment payment;

  private Instant createdAt;
}
