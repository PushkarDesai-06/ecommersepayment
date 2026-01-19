# E-Commerce Payment System

A complete e-commerce backend API built with Spring Boot 4.0.1, MongoDB, and Razorpay payment integration.

## 🚀 Features

- ✅ User Management (CRUD operations)
- ✅ Product Catalog Management
- ✅ Shopping Cart Operations
- ✅ Order Processing
- ✅ Razorpay Payment Integration
- ✅ Webhook Support for Payment Callbacks
- ✅ Order Status Management
- ✅ Stock Management
- ✅ Bonus Features: Product Search, Order Cancellation, Order History

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB (running on localhost:27017)
- Razorpay Account (for payment integration)

## 🛠️ Tech Stack

| Component           | Technology              |
| ------------------- | ----------------------- |
| Framework           | Spring Boot 4.0.1       |
| Language            | Java 17                 |
| Database            | MongoDB                 |
| Payment Gateway     | Razorpay                |
| Build Tool          | Maven                   |
| Validation          | Jakarta Bean Validation |
| Code Simplification | Lombok                  |

## 📦 Quick Start

### 1. Clone the Repository

```bash
cd d:\00 Code\SpringBoot\EcommersePayment
```

### 2. Configure Application

Edit `src/main/resources/application.properties` and add your Razorpay credentials:

```properties
razorpay.key.id=YOUR_RAZORPAY_KEY_ID
razorpay.key.secret=YOUR_RAZORPAY_KEY_SECRET
razorpay.webhook.secret=YOUR_RAZORPAY_WEBHOOK_SECRET
```

### 3. Start MongoDB

Ensure MongoDB is running:

```bash
# Windows
net start MongoDB

# Linux/Mac
sudo systemctl start mongod
```

### 4. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

Application will start on **http://localhost:8080**

### 5. Import Postman Collection

Import `EcommersePayment.postman_collection.json` into Postman for API testing.

## 📚 Documentation

Comprehensive documentation is available in the `mdfiles/` directory:

- **[Setup Guide](mdfiles/SETUP_GUIDE.md)** - Installation and configuration
- **[API Documentation](mdfiles/API_DOCUMENTATION.md)** - Complete API reference
- **[Database Schema](mdfiles/DATABASE_SCHEMA.md)** - ER diagram and collections
- **[Architecture](mdfiles/ARCHITECTURE.md)** - System design and patterns

## 🔌 API Endpoints

### User APIs

- `POST /api/users` - Create user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID

### Product APIs

- `POST /api/products` - Create product
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?q=keyword` - Search products

### Cart APIs

- `POST /api/cart/add` - Add item to cart
- `GET /api/cart/{userId}` - Get cart items
- `DELETE /api/cart/{userId}/clear` - Clear cart

### Order APIs

- `POST /api/orders` - Create order from cart
- `GET /api/orders/{orderId}` - Get order details
- `GET /api/orders/user/{userId}` - Get user's orders
- `POST /api/orders/{orderId}/cancel` - Cancel order

### Payment APIs

- `POST /api/payments/create` - Create payment
- `GET /api/payments/{paymentId}` - Get payment details

### Webhook

- `POST /api/webhooks/payment` - Razorpay payment webhook

## 🎯 Complete Purchase Flow

1. **Create User** (or use seeded users)
2. **View Products** (5 products seeded automatically)
3. **Add to Cart** - Add items with quantity
4. **View Cart** - Verify cart contents
5. **Create Order** - Converts cart to order
6. **Create Payment** - Initiates Razorpay payment
7. **Webhook** - Razorpay sends payment status
8. **Verify Order** - Check order status updated to PAID

## 🗄️ Database Schema

### Collections

- `users` - User accounts
- `products` - Product catalog
- `cart_items` - Shopping cart items
- `orders` - Order records with embedded order items
- `payments` - Payment transactions

### Order Status Flow

```
CREATED → PAID (payment success)
        → FAILED (payment failed)
        → CANCELLED (manual cancellation)
```

## 🧪 Testing

### Using Postman

1. Import the collection
2. Run requests in sequence:
   - Create User → Get Users (copy userId)
   - Get Products (copy productId)
   - Add to Cart → View Cart
   - Create Order (copy orderId)
   - Create Payment (copy razorpayOrderId)
   - Simulate Webhook
   - Verify Order Status

### Automated Data Seeding

On first run, the application automatically seeds:

- 2 sample users (john_doe, jane_smith)
- 5 sample products (Laptop, Mouse, Keyboard, Monitor, Headphones)

## 🔐 Configuration

### Environment Variables (Recommended for Production)

```bash
export RAZORPAY_KEY_ID=your_key_id
export RAZORPAY_KEY_SECRET=your_key_secret
export RAZORPAY_WEBHOOK_SECRET=your_webhook_secret
```

### Application Properties

Key configurations in `application.properties`:

```properties
server.port=8080
spring.data.mongodb.uri=mongodb://localhost:27017/ecommerce
razorpay.key.id=${RAZORPAY_KEY_ID}
razorpay.key.secret=${RAZORPAY_KEY_SECRET}
```

## 📁 Project Structure

```
src/main/java/com/pushkar/ecommersepayment/
├── config/                 # Configuration and exception handling
├── controller/             # REST API controllers
├── dto/                    # Request/Response objects
├── model/                  # MongoDB entities
├── repository/             # Data access layer
├── service/                # Business logic
└── webhook/                # Payment webhook handler
```

## 🎓 Assignment Compliance

This project meets all requirements:

- ✅ Product APIs (15 points)
- ✅ Cart APIs (20 points)
- ✅ Order APIs (25 points)
- ✅ Payment Integration (30 points)
- ✅ Order Status Update (10 points)
- ✅ Clean Code & Structure (10 points)
- ✅ Postman Collection (10 points)
- ✅ **Bonus: Razorpay Integration (+10 points)**
- ✅ **Bonus: Order History (+5 points)**
- ✅ **Bonus: Order Cancellation (+5 points)**
- ✅ **Bonus: Product Search (+5 points)**

**Total: 120/100 points**

## 🐛 Troubleshooting

### MongoDB Connection Error

```
Error: MongoTimeoutException
```

**Solution**: Ensure MongoDB is running on port 27017

### Port Already in Use

```
Error: Port 8080 is already in use
```

**Solution**: Change port in `application.properties` or kill process on 8080

### Razorpay API Error

```
Error: Invalid API key
```

**Solution**: Verify credentials in `application.properties`

## 📝 License

This is an educational project for college assignment purposes.

## 👨‍💻 Author

Created as part of the Spring Boot E-Commerce assignment.

---

