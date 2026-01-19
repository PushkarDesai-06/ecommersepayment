package com.pushkar.ecommersepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookRequest {

  private String orderId;

  private String paymentId;

  private String status;

  private String signature;
}
