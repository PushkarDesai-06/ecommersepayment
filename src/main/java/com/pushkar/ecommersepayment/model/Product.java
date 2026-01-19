package com.pushkar.ecommersepayment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

  @Id
  private String id;

  private String name;

  private String description;

  private Double price;

  private Integer stock;
}
