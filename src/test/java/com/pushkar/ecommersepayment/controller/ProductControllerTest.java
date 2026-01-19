package com.pushkar.ecommersepayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pushkar.ecommersepayment.model.Product;
import com.pushkar.ecommersepayment.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ProductService productService;

  private Product testProduct;

  @BeforeEach
  void setUp() {
    testProduct = new Product();
    testProduct.setId("prod123");
    testProduct.setName("Laptop");
    testProduct.setDescription("Gaming Laptop");
    testProduct.setPrice(50000.0);
    testProduct.setStock(10);
  }

  @Test
  @DisplayName("Should create product via API")
  void testCreateProduct() throws Exception {
    when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

    mockMvc.perform(post("/api/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(testProduct)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is("prod123")))
        .andExpect(jsonPath("$.name", is("Laptop")))
        .andExpect(jsonPath("$.price", is(50000.0)))
        .andExpect(jsonPath("$.stock", is(10)));

    verify(productService, times(1)).createProduct(any(Product.class));
  }

  @Test
  @DisplayName("Should get all products")
  void testGetAllProducts() throws Exception {
    Product product2 = new Product();
    product2.setId("prod456");
    product2.setName("Mouse");
    product2.setPrice(1000.0);

    when(productService.getAllProducts()).thenReturn(Arrays.asList(testProduct, product2));

    mockMvc.perform(get("/api/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("Laptop")))
        .andExpect(jsonPath("$[1].name", is("Mouse")));
  }

  @Test
  @DisplayName("Should search products by name")
  void testSearchProducts() throws Exception {
    when(productService.searchProducts("laptop"))
        .thenReturn(Arrays.asList(testProduct));

    mockMvc.perform(get("/api/products/search")
        .param("q", "laptop"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", containsStringIgnoringCase("laptop")));
  }
}
