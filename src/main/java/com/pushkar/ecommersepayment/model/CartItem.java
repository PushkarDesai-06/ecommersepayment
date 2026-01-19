package com.pushkar.ecommersepayment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cart_items")
public class CartItem {

  @Id
  private String id;

  private String userId;

  private String productId;

  private Integer quantity;
}
