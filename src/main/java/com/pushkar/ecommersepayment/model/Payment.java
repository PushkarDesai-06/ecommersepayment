package com.pushkar.ecommersepayment.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

  @Id
  private String id;

  private String orderId;

  private Double amount;

  private String status; // PENDING, SUCCESS, FAILED

  private String paymentId; // External payment ID (Razorpay)

  private String razorpayOrderId;

  private Instant createdAt;
}
