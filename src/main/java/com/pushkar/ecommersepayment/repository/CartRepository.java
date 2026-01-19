package com.pushkar.ecommersepayment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pushkar.ecommersepayment.model.CartItem;

@Repository
public interface CartRepository extends MongoRepository<CartItem, String> {
  List<CartItem> findByUserId(String userId);

  Optional<CartItem> findByUserIdAndProductId(String userId, String productId);

  void deleteByUserId(String userId);
}
