package com.pushkar.ecommersepayment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pushkar.ecommersepayment.dto.PaymentRequest;
import com.pushkar.ecommersepayment.model.Payment;
import com.pushkar.ecommersepayment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/create")
  public ResponseEntity<Payment> createPayment(@Valid @RequestBody PaymentRequest request) {
    Payment payment = paymentService.createPayment(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(payment);
  }

  @GetMapping("/{paymentId}")
  public ResponseEntity<Payment> getPaymentById(@PathVariable String paymentId) {
    Payment payment = paymentService.getPaymentById(paymentId);
    return ResponseEntity.ok(payment);
  }
}
