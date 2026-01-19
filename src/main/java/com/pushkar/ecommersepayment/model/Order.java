package com.pushkar.ecommersepayment.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

  @Id
  private String id;

  private String userId;

  private Double totalAmount;

  private String status; // CREATED, PAID, FAILED, CANCELLED

  private List<OrderItem> items = new ArrayList<>();

  private Instant createdAt;
}
