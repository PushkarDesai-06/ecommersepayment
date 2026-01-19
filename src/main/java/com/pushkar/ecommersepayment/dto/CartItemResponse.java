package com.pushkar.ecommersepayment.dto;

import com.pushkar.ecommersepayment.model.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

  private String id;

  private String userId;

  private String productId;

  private Integer quantity;

  private Product product;
}
