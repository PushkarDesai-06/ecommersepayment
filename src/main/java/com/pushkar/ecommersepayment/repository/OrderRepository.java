package com.pushkar.ecommersepayment.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pushkar.ecommersepayment.model.Order;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
  List<Order> findByUserId(String userId);
}
