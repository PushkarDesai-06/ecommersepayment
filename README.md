# E-Commerce Payment System

A production-ready e-commerce backend API with Spring Boot 4.0.1, MongoDB, and a custom Mock Payment Service that simulates real payment gateway behavior.

## 🎯 What This Project Does

This is a complete e-commerce backend that handles:

- User and product management
- Shopping cart with real-time stock validation
- Order creation and processing
- Payment simulation with async webhook callbacks
- Complete order lifecycle management

**Key Implementation**: Custom mock payment service that replicates payment gateway behavior without external dependencies.

## 🛠️ Technology Stack

| Component       | Technology              |
| --------------- | ----------------------- |
| Framework       | Spring Boot 4.0.1       |
| Language        | Java 17                 |
| Database        | MongoDB 8.0             |
| Payment System  | Custom Mock Service     |
| Build Tool      | Maven 3.11              |
| Validation      | Jakarta Bean Validation |
| Code Generation | Lombok                  |

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB running on localhost:27017

### Build and Run

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/EcommersePayment-0.0.1-SNAPSHOT.jar
```

Application starts on **http://localhost:8080**

### Automated Setup

On first startup, the application automatically:

- Creates database schema
- Seeds 2 test users (john_doe, jane_smith)
- Seeds 5 products (Laptop ₹50,000, Mouse ₹1,000, Keyboard ₹3,000, Monitor ₹20,000, Headphones ₹5,000)

### Import Postman Collection

The included `EcommersePayment.postman_collection.json` contains all API requests with auto-capturing variables.

## 🎯 Core Features Implemented

### 1. Complete E-Commerce Flow

- User registration and management
- Product catalog with stock tracking
- Shopping cart with quantity updates
- Order creation from cart
- Payment processing with async callbacks
- Order status management

### 2. Business Logic

- **Stock Validation**: Prevents overselling by checking stock before adding to cart and creating orders
- **Cart to Order Conversion**: Automatically converts cart items to order items and clears cart
- **Stock Deduction**: Reduces product stock when order is created
- **Stock Restoration**: Returns stock when order is cancelled

### 3. Data Relationships

- **One-to-Many**: User → Orders (one user can have multiple orders)
- **Many-to-Many**: Order ↔ Products (via OrderItem embedded documents)
- **Referenced Collections**: Cart references User and Product IDs

### 4. Additional Features

- Product search by name (case-insensitive)
- Order history per user
- Order cancellation with stock restoration
- Global exception handling with meaningful error messages

## � Mock Payment Service - Implementation Details

### Why Mock Payment?

Instead of integrating external payment gateways (Razorpay, Stripe, etc.), I implemented a custom mock payment service that simulates real payment gateway behavior. This approach provides:

- **No External Dependencies**: Works offline, no API keys needed
- **Controlled Testing**: Configurable success/failure rates
- **Realistic Behavior**: Async processing with webhooks, just like real gateways
- **Educational Value**: Demonstrates understanding of payment gateway patterns

### How It Works

#### 1. Payment Creation

When a payment is created via `POST /api/payments/create`:

```java
// Generate mock payment identifiers
String mockOrderId = "mock_order_" + UUID.randomUUID();
String mockPaymentId = "mock_pay_" + UUID.randomUUID();

// Save payment with PENDING status
Payment payment = new Payment();
payment.setOrderId(orderId);
payment.setStatus("PENDING");
payment.setRazorpayOrderId(mockOrderId);
payment.setPaymentId(mockPaymentId);
```

**Response**: Returns payment object immediately with PENDING status.

#### 2. Async Processing

The mock service spawns a background thread to simulate payment gateway processing:

```java
new Thread(() -> {
    // Simulate network delay (configurable)
    Thread.sleep(mockDelay); // default: 2000ms

    // Random success/failure based on probability
    boolean success = Math.random() > failureProbability; // default: 0.1 (10% failure)

    if (success) {
        handlePaymentSuccess(mockPaymentId, mockOrderId, null);
    } else {
        handlePaymentFailure(mockOrderId, "Mock payment failure");
    }
}).start();
```

This mimics how real payment gateways:

- Process payments asynchronously
- Take time to respond
- Can succeed or fail

#### 3. Webhook Callbacks

After processing, the mock service internally calls webhook handlers (just like Razorpay would):

**On Success:**

```java
// Update payment status
payment.setStatus("SUCCESS");
paymentRepository.save(payment);

// Update order status
order.setStatus("PAID");
orderRepository.save(order);
```

**On Failure:**

```java
// Update payment status
payment.setStatus("FAILED");
paymentRepository.save(payment);

// Update order status
order.setStatus("FAILED");
orderRepository.save(order);
```

#### 4. Manual Webhook Testing

The webhook endpoint `POST /api/webhooks/payment` accepts manual triggers:

**Success Webhook:**

```json
{
  "event": "payment.captured",
  "paymentId": "mock_pay_12345",
  "orderId": "mock_order_67890"
}
```

**Failure Webhook:**

```json
{
  "event": "payment.failed",
  "orderId": "mock_order_67890",
  "reason": "Insufficient funds"
}
```

### Configuration

File: `src/main/resources/application.properties`

```properties
# Enable/disable mock payment
payment.mock.enabled=true

# Delay before processing (milliseconds)
payment.mock.success.delay=2000

# Failure probability (0.0 = always success, 1.0 = always fail)
payment.mock.failure.probability=0.1
```

### Testing the Payment Flow

1. **Create Order**: `POST /api/orders` → Returns order with status `CREATED`
2. **Create Payment**: `POST /api/payments/create` → Returns payment with status `PENDING`
3. **Wait 2 seconds**: Mock service processes in background
4. **Check Order**: `GET /api/orders/{orderId}` → Status changed to `PAID` (90%) or `FAILED` (10%)

### Payment Status Flow

```
PENDING → [2 second delay] → SUCCESS (90%) → Order: PAID
                           → FAILED (10%)  → Order: FAILED
```

### Key Implementation Points

1. **UUID Generation**: Each payment gets unique mock IDs for tracking
2. **Thread Safety**: Uses `@Transactional` annotations for database consistency
3. **Logging**: Comprehensive logs for debugging payment flow
4. **Error Handling**: Proper exception handling for edge cases
5. **Status Management**: Maintains consistency between Payment and Order statuses

## 🔌 API Endpoints

### User Management

- `POST /api/users` - Create new user
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Product Management

- `POST /api/products` - Create product
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?q=keyword` - Search products by name
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Shopping Cart

- `POST /api/cart/add` - Add item to cart
  ```json
  {
    "userId": "678a123...",
    "productId": "678a456...",
    "quantity": 2
  }
  ```
- `GET /api/cart/{userId}` - Get cart items with product details
- `DELETE /api/cart/{userId}/clear` - Clear entire cart
- `DELETE /api/cart/item/{cartItemId}` - Remove specific item

### Order Management

- `POST /api/orders` - Create order from cart
  ```json
  {
    "userId": "678a123..."
  }
  ```
- `GET /api/orders/{orderId}` - Get order with payment details
- `GET /api/orders/user/{userId}` - Get user's order history
- `POST /api/orders/{orderId}/cancel` - Cancel order (restores stock)

### Payment Processing

- `POST /api/payments/create` - Initiate payment
  ```json
  {
    "orderId": "678a789...",
    "amount": 100000.0
  }
  ```
- `GET /api/payments/{paymentId}` - Get payment status

### Webhook

- `POST /api/webhooks/payment` - Payment callback (simulates gateway webhook)
  ```json
  {
    "event": "payment.captured",
    "paymentId": "mock_pay_...",
    "orderId": "mock_order_..."
  }
  ```

## 📊 Complete Purchase Flow

Here's how a complete transaction works:

### 1. Setup Phase

```bash
# Get available users (seeded automatically)
GET /api/users

# Get available products
GET /api/products
```

### 2. Shopping Phase

```bash
# Add Laptop to cart
POST /api/cart/add
{
  "userId": "<user_id>",
  "productId": "<laptop_id>",
  "quantity": 1
}

# Add Mouse to cart
POST /api/cart/add
{
  "userId": "<user_id>",
  "productId": "<mouse_id>",
  "quantity": 2
}

# View cart
GET /api/cart/<user_id>
```

### 3. Order Phase

```bash
# Create order (converts cart to order)
POST /api/orders
{
  "userId": "<user_id>"
}

# Response includes:
# - Order ID
# - Total amount
# - Order items
# - Status: "CREATED"
# - Stock deducted from products
# - Cart cleared
```

### 4. Payment Phase

```bash
# Initiate payment
POST /api/payments/create
{
  "orderId": "<order_id>",
  "amount": 52000.0
}

# Response includes:
# - Payment ID
# - Mock order ID
# - Status: "PENDING"
```

### 5. Processing Phase (Automatic)

```
[Wait 2 seconds]

Mock service processes payment in background:
- 90% chance: Payment SUCCESS → Order status: PAID
- 10% chance: Payment FAILED → Order status: FAILED
```

### 6. Verification Phase

```bash
# Check final order status
GET /api/orders/<order_id>

# Response shows:
# - Order status: "PAID" or "FAILED"
# - Payment details embedded
# - All order items
```

## 🗄️ Database Schema

### Collections

**users**

```json
{
  "_id": "ObjectId",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "customer"
}
```

**products**

```json
{
  "_id": "ObjectId",
  "name": "Laptop",
  "description": "Gaming Laptop",
  "price": 50000.0,
  "stock": 10
}
```

**cart_items**

```json
{
  "_id": "ObjectId",
  "userId": "user_id_reference",
  "productId": "product_id_reference",
  "quantity": 2
}
```

**orders** (with embedded items)

```json
{
  "_id": "ObjectId",
  "userId": "user_id_reference",
  "totalAmount": 102000.0,
  "status": "PAID",
  "items": [
    {
      "productId": "product_id",
      "quantity": 2,
      "price": 50000.0
    }
  ],
  "createdAt": "2026-01-19T12:00:00Z"
}
```

**payments**

```json
{
  "_id": "ObjectId",
  "orderId": "order_id_reference",
  "amount": 102000.0,
  "status": "SUCCESS",
  "paymentId": "mock_pay_uuid",
  "razorpayOrderId": "mock_order_uuid",
  "createdAt": "2026-01-19T12:00:00Z"
}
```

### Order Status Lifecycle

```
CREATED  → [Payment Success] → PAID
         → [Payment Failed]  → FAILED
         → [User Cancels]    → CANCELLED (stock restored)
```

## 🧪 Testing

### Using Postman

1. Import `EcommersePayment.postman_collection.jsonv2`
2. Requests are organized in folders matching the purchase flow
3. Test scripts automatically capture IDs to collection variables
4. Run requests sequentially or use Collection Runner

## 📁 Project Architecture

```
src/main/java/com/pushkar/ecommersepayment/
├── config/
│   ├── DataSeeder.java           # Automatic test data seeding
│   └── GlobalExceptionHandler.java  # Centralized error handling
├── controller/
│   ├── UserController.java
│   ├── ProductController.java
│   ├── CartController.java
│   ├── OrderController.java
│   └── PaymentController.java
├── dto/
│   ├── AddToCartRequest.java     # Request objects with validation
│   ├── CreateOrderRequest.java
│   ├── PaymentRequest.java
│   ├── CartItemResponse.java     # Response objects
│   ├── OrderResponse.java
│   └── PaymentWebhookRequest.java
├── model/
│   ├── User.java                  # MongoDB entities
│   ├── Product.java
│   ├── CartItem.java
│   ├── Order.java                 # Contains embedded OrderItems
│   ├── OrderItem.java             # Embedded in Order
│   └── Payment.java
├── repository/
│   ├── UserRepository.java        # Spring Data MongoDB
│   ├── ProductRepository.java
│   ├── CartRepository.java
│   ├── OrderRepository.java
│   └── PaymentRepository.java
├── service/
│   ├── UserService.java           # Business logic
│   ├── ProductService.java
│   ├── CartService.java
│   ├── OrderService.java          # Handles cart→order conversion
│   └── PaymentService.java        # Mock payment processing
└── webhook/
    └── PaymentWebhookController.java  # Payment callbacks
```

### Design Patterns Used

1. **Repository Pattern**: Data access abstraction
2. **DTO Pattern**: Separate API contracts from domain models
3. **Service Layer**: Encapsulated business logic
4. **Dependency Injection**: Loose coupling via Spring
5. **Builder Pattern**: Lombok @Data, @Builder for object creation

## 🔧 Configuration

### application.properties

```properties
# Server
server.port=8080

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/ecommerce
spring.data.mongodb.database=ecommerce

# Mock Payment Configuration
payment.mock.enabled=true
payment.mock.success.delay=2000
payment.mock.failure.probability=0.1

# Logging
logging.level.com.pushkar.ecommersepayment=DEBUG
logging.level.org.springframework.web=INFO
```

### Key Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MongoDB -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```


## 📚 Additional Documentation


- **Postman Collection** - Includes full API documentation in requests

## 📋 Project Submission

This project implements a complete e-commerce backend system with:

✅ **User Management** - Full CRUD operations
✅ **Product Catalog** - CRUD + search functionality
✅ **Shopping Cart** - Add, update, remove items with stock validation
✅ **Order Processing** - Cart to order conversion with embedded items
✅ **Payment System** - Custom mock service with async processing
✅ **Webhook Handling** - Payment callback simulation
✅ **Stock Management** - Real-time stock tracking and restoration
✅ **Order Lifecycle** - Status updates (CREATED → PAID/FAILED/CANCELLED)
✅ **Exception Handling** - Global error handling with meaningful messages
✅ **Data Validation** - Jakarta Bean Validation on all inputs
✅ **Automated Testing** - Unit and integration tests
✅ **API Documentation** - Postman collection with examples
✅ **Data Seeding** - Automatic test data on startup

### Technical Highlights

- **Clean Architecture**: Separation of concerns with layered design
- **RESTful Design**: Proper HTTP methods, status codes, and resource naming
- **Database Design**: Efficient schema with embedded documents (OrderItem in Order)
- **Transaction Management**: @Transactional for data consistency
- **Async Processing**: Background threads for payment simulation
- **Logging**: Comprehensive logging for debugging and monitoring

---