package com.pushkar.ecommersepayment.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pushkar.ecommersepayment.model.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
  List<Product> findByNameContainingIgnoreCase(String name);
}
