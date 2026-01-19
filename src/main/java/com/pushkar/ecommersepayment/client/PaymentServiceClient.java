package com.pushkar.ecommersepayment.client;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pushkar.ecommersepayment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

  private final PaymentService paymentService;

  @PostMapping("/payment")
  public ResponseEntity<Map<String, String>> handlePaymentWebhook(
      @RequestBody Map<String, Object> payload) {

    log.info("Received mock payment webhook: {}", payload);

    try {
      // Extract event type
      String event = (String) payload.get("event");

      if (event == null) {
        log.error("Event type not found in webhook payload");
        return ResponseEntity.badRequest().body(Map.of("message", "Event type not found"));
      }

      // Process based on event type
      if (event.equals("payment.captured") || event.equals("order.paid")) {
        handlePaymentSuccess(payload);
      } else if (event.equals("payment.failed")) {
        handlePaymentFailure(payload);
      } else {
        log.warn("Unhandled event type: {}", event);
      }

      return ResponseEntity.ok(Map.of("message", "Webhook processed successfully"));

    } catch (Exception e) {
      log.error("Error processing webhook: {}", e.getMessage(), e);
      return ResponseEntity.status(500).body(Map.of("message", "Webhook processing failed"));
    }
  }

  private void handlePaymentSuccess(Map<String, Object> payload) {
    try {
      // Simplified mock payload structure
      String paymentId = (String) payload.get("paymentId");
      String orderId = (String) payload.get("orderId");

      log.info("Processing payment success - Payment ID: {}, Order ID: {}", paymentId, orderId);

      paymentService.handlePaymentSuccess(paymentId, orderId, null);

    } catch (Exception e) {
      log.error("Error handling payment success: {}", e.getMessage(), e);
    }
  }

  private void handlePaymentFailure(Map<String, Object> payload) {
    try {
      // Simplified mock payload structure
      String orderId = (String) payload.get("orderId");
      String errorReason = (String) payload.get("reason");

      log.info("Processing payment failure - Order ID: {}, Reason: {}", orderId, errorReason);

      paymentService.handlePaymentFailure(orderId, errorReason);

    } catch (Exception e) {
      log.error("Error handling payment failure: {}", e.getMessage(), e);
    }
  }
}
