package com.pushkar.ecommersepayment.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pushkar.ecommersepayment.model.Payment;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
  Optional<Payment> findByOrderId(String orderId);

  Optional<Payment> findByPaymentId(String paymentId);

  Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}
