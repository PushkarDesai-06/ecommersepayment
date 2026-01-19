package com.pushkar.ecommersepayment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pushkar.ecommersepayment.dto.AddToCartRequest;
import com.pushkar.ecommersepayment.dto.CreateOrderRequest;
import com.pushkar.ecommersepayment.model.Order;
import com.pushkar.ecommersepayment.model.Product;
import com.pushkar.ecommersepayment.model.User;
import com.pushkar.ecommersepayment.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.data.mongodb.database=ecommerce_test"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("E-Commerce Integration Tests - Complete Flow")
class EcommerceIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CartRepository cartRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private PaymentRepository paymentRepository;

  private static String userId;
  private static String productId1;
  private static String productId2;
  private static String productId3;
  private static String orderId;

  @BeforeEach
  void setUp() {
    // Clean up test data
    cartRepository.deleteAll();
    orderRepository.deleteAll();
    paymentRepository.deleteAll();
  }

  @Test
  @org.junit.jupiter.api.Order(1)
  @DisplayName("1. Create test user")
  void testCreateUser() throws Exception {
    User user = new User();
    user.setUsername("test_user");
    user.setEmail("test@example.com");
    user.setRole("customer");

    MvcResult result = mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(user)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username", is("test_user")))
        .andExpect(jsonPath("$.email", is("test@example.com")))
        .andReturn();

    String response = result.getResponse().getContentAsString();
    User createdUser = objectMapper.readValue(response, User.class);
    userId = createdUser.getId();
  }

  @Test
  @org.junit.jupiter.api.Order(2)
  @DisplayName("2. Create test products - Laptop, Mouse, Keyboard")
  void testCreateProducts() throws Exception {
    // Create Laptop
    Product laptop = new Product();
    laptop.setName("Laptop");
    laptop.setDescription("Gaming Laptop");
    laptop.setPrice(50000.0);
    laptop.setStock(10);

    MvcResult result1 = mockMvc.perform(post("/api/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(laptop)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("Laptop")))
        .andExpect(jsonPath("$.price", is(50000.0)))
        .andExpect(jsonPath("$.stock", is(10)))
        .andReturn();

    Product created1 = objectMapper.readValue(
        result1.getResponse().getContentAsString(), Product.class);
    productId1 = created1.getId();

    // Create Mouse
    Product mouse = new Product();
    mouse.setName("Mouse");
    mouse.setDescription("Wireless Mouse");
    mouse.setPrice(1000.0);
    mouse.setStock(50);

    MvcResult result2 = mockMvc.perform(post("/api/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(mouse)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("Mouse")))
        .andReturn();

    Product created2 = objectMapper.readValue(
        result2.getResponse().getContentAsString(), Product.class);
    productId2 = created2.getId();

    // Create Keyboard
    Product keyboard = new Product();
    keyboard.setName("Keyboard");
    keyboard.setDescription("Mechanical Keyboard");
    keyboard.setPrice(3000.0);
    keyboard.setStock(30);

    MvcResult result3 = mockMvc.perform(post("/api/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(keyboard)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("Keyboard")))
        .andReturn();

    Product created3 = objectMapper.readValue(
        result3.getResponse().getContentAsString(), Product.class);
    productId3 = created3.getId();
  }

  @Test
  @org.junit.jupiter.api.Order(3)
  @DisplayName("3. Add products to cart")
  void testAddToCart() throws Exception {
    // Add Laptop to cart
    AddToCartRequest request1 = new AddToCartRequest();
    request1.setUserId(userId);
    request1.setProductId(productId1);
    request1.setQuantity(2);

    mockMvc.perform(post("/api/cart/add")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request1)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId", is(userId)))
        .andExpect(jsonPath("$.productId", is(productId1)))
        .andExpect(jsonPath("$.quantity", is(2)));

    // Add Mouse to cart
    AddToCartRequest request2 = new AddToCartRequest();
    request2.setUserId(userId);
    request2.setProductId(productId2);
    request2.setQuantity(5);

    mockMvc.perform(post("/api/cart/add")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.quantity", is(5)));
  }

  @Test
  @org.junit.jupiter.api.Order(4)
  @DisplayName("4. View cart with product details - Database Relationship")
  void testViewCart() throws Exception {
    mockMvc.perform(get("/api/cart/" + userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].product.name", notNullValue()))
        .andExpect(jsonPath("$[0].product.price", notNullValue()));
  }

  @Test
  @org.junit.jupiter.api.Order(5)
  @DisplayName("5. Create order from cart - Business Logic Test")
  void testCreateOrder() throws Exception {
    CreateOrderRequest request = new CreateOrderRequest();
    request.setUserId(userId);

    MvcResult result = mockMvc.perform(post("/api/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId", is(userId)))
        .andExpect(jsonPath("$.status", is("CREATED")))
        .andExpect(jsonPath("$.totalAmount", is(105000.0))) // 2*50000 + 5*1000
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andReturn();

    Order createdOrder = objectMapper.readValue(
        result.getResponse().getContentAsString(), Order.class);
    orderId = createdOrder.getId();
  }

  @Test
  @org.junit.jupiter.api.Order(6)
  @DisplayName("6. Verify cart is cleared after order creation")
  void testCartClearedAfterOrder() throws Exception {
    mockMvc.perform(get("/api/cart/" + userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @org.junit.jupiter.api.Order(7)
  @DisplayName("7. Verify stock is deducted - Stock Management")
  void testStockDeduction() throws Exception {
    mockMvc.perform(get("/api/products/" + productId1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stock", is(8))); // 10 - 2

    mockMvc.perform(get("/api/products/" + productId2))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stock", is(45))); // 50 - 5
  }

  @Test
  @org.junit.jupiter.api.Order(8)
  @DisplayName("8. Simulate payment webhook - Webhook Pattern")
  void testPaymentWebhook() throws Exception {
    String webhookPayload = """
        {
          "event": "payment.captured",
          "payload": {
            "payment": {
              "entity": {
                "id": "pay_test123",
                "order_id": "order_test456",
                "status": "captured",
                "amount": 10500000
              }
            }
          }
        }
        """;

    mockMvc.perform(post("/api/webhooks/payment")
        .contentType(MediaType.APPLICATION_JSON)
        .content(webhookPayload))
        .andExpect(status().isOk());
  }

  @Test
  @org.junit.jupiter.api.Order(9)
  @DisplayName("9. Get order with payment details")
  void testGetOrderWithPayment() throws Exception {
    mockMvc.perform(get("/api/orders/" + orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(orderId)))
        .andExpect(jsonPath("$.userId", is(userId)))
        .andExpect(jsonPath("$.totalAmount", is(105000.0)))
        .andExpect(jsonPath("$.items", hasSize(2)));
  }

  @Test
  @org.junit.jupiter.api.Order(10)
  @DisplayName("10. Get user order history - One-to-Many Relationship")
  void testGetOrderHistory() throws Exception {
    mockMvc.perform(get("/api/orders/user/" + userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[0].userId", is(userId)));
  }

  @Test
  @org.junit.jupiter.api.Order(11)
  @DisplayName("11. Search products")
  void testSearchProducts() throws Exception {
    mockMvc.perform(get("/api/products/search")
        .param("q", "laptop"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[0].name", containsStringIgnoringCase("laptop")));
  }

  @Test
  @org.junit.jupiter.api.Order(12)
  @DisplayName("12. Test insufficient stock scenario")
  void testInsufficientStock() throws Exception {
    AddToCartRequest request = new AddToCartRequest();
    request.setUserId(userId);
    request.setProductId(productId1);
    request.setQuantity(100); // More than available stock

    mockMvc.perform(post("/api/cart/add")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("Insufficient stock")));
  }
}
