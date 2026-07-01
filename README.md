# 🛒 Online Shop REST API

A production-ready RESTful API for an e-commerce platform built with **Spring Boot**, featuring JWT authentication, role-based access control, cart management, order processing, and **Midtrans** payment gateway integration.

---

## 📌 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Run with Docker](#run-with-docker-recommended)
  - [Run Locally](#run-locally)
- [Environment Variables](#-environment-variables)
- [API Documentation](#-api-documentation)
  - [Auth](#-auth)
  - [Category](#-category)
  - [Product](#-product)
  - [Cart](#-cart)
  - [Order](#-order)
  - [Payment](#-payment)
- [End-to-End Testing Flow](#-end-to-end-testing-flow)
- [Default Credentials](#-default-credentials)
- [Database ERD](#-database-erd)
- [Project Structure](#-project-structure)

---

## ✨ Features

- 🔐 **JWT Authentication** — Access token + Refresh token flow
- 👥 **Role-based Access Control** — `ADMIN` & `CUSTOMER` roles
- 🛒 **Cart Management** — Add, update, remove items with auto quantity merge
- 📦 **Order Processing** — Checkout from cart with automatic stock deduction
- ⚡ **Optimistic Locking** — Prevents overselling on concurrent checkout
- 💳 **Midtrans Snap Integration** — Redirect-based payment with webhook verification
- 🔒 **Signature Verification** — SHA-512 validation on every Midtrans notification
- 🐳 **Docker Ready** — One-command setup with Docker Compose
- 🧹 **Clean Architecture** — Layered architecture with Uncle Bob principles (Service interface + ServiceImpl separation)
- 📋 **Global Exception Handling** — Consistent error response format across all endpoints

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Payment | Midtrans Snap API |
| HTTP Client | RestTemplate |
| Build Tool | Maven |
| Containerization | Docker + Docker Compose |
| Utilities | Lombok, Bean Validation |

---

## 🏗 Architecture

This project follows **Clean Architecture / Layered Architecture** principles:

```
Controller → Service (interface) → ServiceImpl → Repository → Entity
```

```
com.onlineshop
├── config/           # Spring & Midtrans configuration, Data Seeder
├── controller/       # REST endpoints (thin layer, no business logic)
├── service/          # Service interfaces (contracts)
│   └── impl/         # Business logic implementation
├── repository/       # JPA Repository interfaces
├── model/            # JPA Entities
├── dto/
│   ├── request/      # Incoming request bodies
│   └── response/     # Outgoing response bodies
├── exception/        # Custom exceptions & Global Exception Handler
└── security/         # JWT utilities & filters
```

**Key design decisions:**
- **Service interface + ServiceImpl separation** enables easy mocking for unit tests and loose coupling between layers
- **DTO pattern** prevents exposing internal entity structure and avoids circular JSON serialization
- **Optimistic Locking (`@Version`)** on `Product` entity handles concurrent checkout race conditions
- **`priceAtPurchase`** stored in `OrderItem` to preserve historical pricing regardless of future product price changes

---

## 🚀 Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (for Docker setup)
- OR Java 21 + Maven + PostgreSQL (for local setup)
- [Postman](https://www.postman.com/) or any API client for testing
- Midtrans Sandbox account ([dashboard.sandbox.midtrans.com](https://dashboard.sandbox.midtrans.com))

---

### Run with Docker (Recommended)

This is the easiest way to run the project without installing Java or PostgreSQL locally.

**1. Clone the repository**
```bash
git clone https://github.com/yourusername/online-shop.git
cd online-shop
```

**2. Create your `.env` file**
```bash
cp .env.example .env
```

Then fill in your actual values in `.env`:
```env
JWT_SECRET=your-secret-key-min-32-chars
MIDTRANS_SERVER_KEY=SB-Mid-server-xxxxxxxxxxxx
MIDTRANS_CLIENT_KEY=SB-Mid-client-xxxxxxxxxxxx
```

> 💡 Generate a secure JWT secret:
> ```bash
> openssl rand -base64 32
> ```

**3. Run with Docker Compose**
```bash
docker-compose up
```

The app will be available at `http://localhost:8080`.

> **Note:** PostgreSQL inside Docker runs on port `5433` to avoid conflict with any local PostgreSQL instance on `5432`.

**Useful Docker commands:**
```bash
# Run in background
docker-compose up -d

# Stop containers
docker-compose down

# Stop and delete all data (fresh start)
docker-compose down -v

# Rebuild after code changes
docker-compose up --build
```

---

### Run Locally

**1. Create PostgreSQL database**
```sql
CREATE DATABASE online_shop_db;
```

**2. Configure `application.properties`**

Update `src/main/resources/application.properties` with your local database credentials and keys.

**3. Run the app**
```bash
mvn spring-boot:run
```

---

## ⚙️ Environment Variables

| Variable | Description | Example |
|---|---|---|
| `JWT_SECRET` | Secret key for signing JWT (min 32 chars) | `openssl rand -base64 32` |
| `MIDTRANS_SERVER_KEY` | Midtrans sandbox server key | `SB-Mid-server-xxx` |
| `MIDTRANS_CLIENT_KEY` | Midtrans sandbox client key | `SB-Mid-client-xxx` |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL (auto-set by Docker) | `jdbc:postgresql://...` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password | `postgres` |

---

## 📡 API Documentation

**Base URL:** `http://localhost:8080`

**Response format** (all endpoints):
```json
{
  "success": true,
  "message": "Description here",
  "data": {}
}
```

**Authentication:** All protected endpoints require:
```
Authorization: Bearer <accessToken>
```

---

### 🔐 Auth

#### Register
```
POST /api/auth/register
```
```json
{
  "name": "Budi Santoso",
  "email": "budi@mail.com",
  "password": "budi123"
}
```
> New users are always registered as `CUSTOMER`. Role cannot be set via request.

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "email": "budi@mail.com",
    "role": "CUSTOMER"
  }
}
```

---

#### Login
```
POST /api/auth/login
```
```json
{
  "email": "budi@mail.com",
  "password": "budi123"
}
```

---

#### Refresh Token
```
POST /api/auth/refresh?refreshToken=<your_refresh_token>
```
> Access token expires in **1 hour**. Use this endpoint to get a new one without re-login.

---

### 🗂 Category

| Method | Endpoint | Auth | Role |
|---|---|---|---|
| GET | `/api/categories` | ❌ Public | - |
| POST | `/api/categories?name=Elektronik` | ✅ Required | ADMIN |
| DELETE | `/api/categories/{id}` | ✅ Required | ADMIN |

---

### 📦 Product

| Method | Endpoint | Auth | Role |
|---|---|---|---|
| GET | `/api/products` | ❌ Public | - |
| GET | `/api/products/{id}` | ❌ Public | - |
| GET | `/api/products/category/{categoryId}` | ❌ Public | - |
| POST | `/api/products` | ✅ Required | ADMIN |
| PUT | `/api/products/{id}` | ✅ Required | ADMIN |
| DELETE | `/api/products/{id}` | ✅ Required | ADMIN |

**Create/Update Product body:**
```json
{
  "name": "Sepatu Sneakers",
  "description": "Sepatu kasual unisex nyaman dipakai sehari-hari",
  "price": 250000,
  "stock": 10,
  "imageUrl": "https://example.com/sepatu.jpg",
  "categoryId": 1
}
```

**Product Response:**
```json
{
  "id": 1,
  "name": "Sepatu Sneakers",
  "description": "Sepatu kasual unisex nyaman dipakai sehari-hari",
  "price": 250000,
  "stock": 10,
  "imageUrl": "https://example.com/sepatu.jpg",
  "categoryName": "Fashion"
}
```

---

### 🛒 Cart

All cart endpoints require authentication (any role).

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/cart` | Get current user's cart |
| POST | `/api/cart/items` | Add item to cart |
| PUT | `/api/cart/items/{cartItemId}?quantity=3` | Update item quantity |
| DELETE | `/api/cart/items/{cartItemId}` | Remove item from cart |

**Add to Cart body:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

> Adding the same product again will **increase quantity**, not create a duplicate item.

**Cart Response:**
```json
{
  "cartId": 1,
  "items": [
    {
      "cartItemId": 1,
      "productId": 1,
      "productName": "Sepatu Sneakers",
      "price": 250000,
      "quantity": 2,
      "subtotal": 500000
    }
  ],
  "totalPrice": 500000
}
```

---

### 📑 Order

All order endpoints require authentication.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders/checkout` | Checkout all items in cart |
| GET | `/api/orders` | Get current user's order history |
| GET | `/api/orders/{id}` | Get specific order detail |

**Checkout** creates a new Order from the current cart, decrements product stock, and clears the cart.

> ⚡ If two users checkout the same product simultaneously and stock runs out, the second request will receive `409 Conflict` (Optimistic Locking).

**Order Response:**
```json
{
  "orderId": 1,
  "status": "PENDING",
  "totalAmount": 500000,
  "createdAt": "2025-01-01T10:00:00",
  "paymentUrl": null,
  "items": [
    {
      "productName": "Sepatu Sneakers",
      "quantity": 2,
      "priceAtPurchase": 250000
    }
  ]
}
```

**Order statuses:** `PENDING` → `PAID` / `FAILED` / `CANCELLED`

---

### 💳 Payment

#### Generate Payment Link
```
POST /api/payment/{orderId}/pay
```
*(Requires authentication)*

Returns a Midtrans Snap `redirect_url`. Open this URL in browser to complete payment using the Midtrans sandbox simulation page.

**Response:**
```json
{
  "success": true,
  "message": "Snap transaction created",
  "data": "https://app.sandbox.midtrans.com/snap/v4/redirection/xxxx"
}
```

---

#### Payment Webhook (Midtrans → Server)
```
POST /api/payment/notification
```
> ⚠️ This endpoint is called **automatically by Midtrans servers** after payment. Do NOT call this manually.

Every notification is verified using **SHA-512 signature** before processing to prevent fraudulent requests.

**Payment statuses:** `PENDING` → `SETTLEMENT` / `FAILED` / `EXPIRED`

---

#### Webhook Setup (Local Development)

Since Midtrans cannot reach `localhost`, use **ngrok** for tunneling:

```bash
ngrok http 8080
```

Then set the notification URL in Midtrans Dashboard:
**Settings → Configuration → Payment Notification URL:**
```
https://your-ngrok-url.ngrok-free.app/api/payment/notification
```

You can monitor incoming webhook requests at `http://localhost:4040` (ngrok dashboard).

---

## 🧪 End-to-End Testing Flow

Follow this sequence in Postman for complete flow testing:

```
1.  POST   /api/auth/login              → Login as admin (admin@shop.com / admin123)
2.  POST   /api/categories?name=Fashion → Create a category (admin token)
3.  POST   /api/products                → Create a product with stock: 5 (admin token)
4.  POST   /api/auth/register           → Register as customer
5.  POST   /api/auth/login              → Login as customer → copy accessToken
6.  GET    /api/products                → Browse products (no token needed)
7.  POST   /api/cart/items              → Add product to cart (customer token)
8.  GET    /api/cart                    → Verify cart contents
9.  POST   /api/orders/checkout         → Checkout → copy orderId
10. POST   /api/payment/{orderId}/pay   → Get payment URL → open in browser
11. [Browser] Complete payment simulation on Midtrans Snap page
12. GET    /api/orders/{id}             → Verify order status is now PAID
```

**Bonus — Test Optimistic Locking:**
1. Create a product with `stock: 1`
2. Register 2 different customer accounts
3. Both add the same product to cart
4. Send `POST /api/orders/checkout` simultaneously from both accounts
5. One should succeed (`200 OK`), the other should get `409 Conflict`

---

## 🔑 Default Credentials

| Role | Email | Password |
|---|---|---|
| ADMIN | admin@shop.com | admin123 |

> Admin account is automatically seeded on first application startup.

---

## 🗃 Database ERD

```
users (1) ──────────── (N) products [created_by]
users (1) ──────────── (1) carts
users (1) ──────────── (N) orders
carts (1) ──────────── (N) cart_items
cart_items (N) ──────── (1) products
orders (1) ──────────── (N) order_items
orders (1) ──────────── (1) payments
order_items (N) ──────── (1) products
categories (1) ──────── (N) products
```

---

## 📁 Project Structure

```
src/main/java/com/onlineshop/
├── config/
│   ├── DataSeeder.java         # Auto-seed admin account on startup
│   ├── MidtransConfig.java     # RestTemplate bean
│   └── SecurityConfig.java     # Spring Security + JWT filter chain
├── controller/
│   ├── AuthController.java
│   ├── CartController.java
│   ├── CategoryController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   └── ProductController.java
├── dto/
│   ├── request/
│   │   ├── AddToCartRequest.java
│   │   ├── LoginRequest.java
│   │   ├── ProductRequest.java
│   │   └── RegisterRequest.java
│   └── response/
│       ├── ApiResponse.java     # Generic wrapper for all responses
│       ├── AuthResponse.java
│       ├── CartResponse.java
│       ├── OrderResponse.java
│       └── ProductResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── InsufficientStockException.java
│   └── ResourceNotFoundException.java
├── model/
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Category.java
│   ├── Order.java               # OrderStatus enum inside
│   ├── OrderItem.java           # Stores priceAtPurchase snapshot
│   ├── Payment.java             # PaymentStatus enum inside
│   ├── Product.java             # @Version for Optimistic Locking
│   └── User.java                # Role enum inside
├── repository/
│   ├── CartItemRepository.java
│   ├── CartRepository.java
│   ├── CategoryRepository.java
│   ├── OrderItemRepository.java
│   ├── OrderRepository.java
│   ├── PaymentRepository.java
│   ├── ProductRepository.java   # findByIdForCheckout with @Lock
│   └── UserRepository.java
├── security/
│   ├── JwtAuthFilter.java       # OncePerRequestFilter for JWT validation
│   ├── JwtUtil.java             # Token generation & validation
│   └── UserDetailsServiceImpl.java
└── service/
    ├── AuthService.java
    ├── CartService.java
    ├── CategoryService.java
    ├── OrderService.java
    ├── PaymentService.java
    ├── ProductService.java
    └── impl/
        ├── AuthServiceImpl.java
        ├── CartServiceImpl.java     # IDOR protection on cart operations
        ├── CategoryServiceImpl.java
        ├── OrderServiceImpl.java    # Optimistic Locking checkout flow
        ├── PaymentServiceImpl.java  # Midtrans Snap + SHA-512 verification
        └── ProductServiceImpl.java
```

---

## 📄 License

This project is built for portfolio purposes.
