# ECOM Microservices Architecture

A hands-on **Spring Boot Microservices** project demonstrating **Service Discovery, Inter-Service Communication using OpenFeign, and API Gateway-based routing**.

The project consists of an API Gateway, Eureka Service Registry, Inventory Service (Producer), and Order Service (Consumer).

---

## Architecture

```text
                         ┌──────────────────┐
                         │      Client      │
                         │   Postman / UI   │
                         └────────┬─────────┘
                                  │
                                  │ HTTP Request
                                  ▼
                         ┌──────────────────┐
                         │   API Gateway    │
                         │      :9090       │
                         │ Static Routing   │
                         └────────┬─────────┘
                                  │
                                  │ Routes Request
                                  ▼
                         ┌──────────────────┐
                         │  Order Service   │
                         │   Consumer       │
                         │      :8082       │
                         └────────┬─────────┘
                                  │
                                  │ OpenFeign
                                  │ Inter-Service
                                  │ Communication
                                  ▼
                         ┌──────────────────┐
                         │ Inventory Service│
                         │    Producer      │
                         │      :8081       │
                         └──────────────────┘


                    ┌─────────────────────────┐
                    │     Eureka Server       │
                    │    Service Registry     │
                    │         :8761           │
                    └─────────────────────────┘
                              ▲       ▲
                              │       │
                         Registration / 
                         Service Discovery
                              │       │
                         ┌────┘       └────┐
                         │                 │
                  Order Service      Inventory Service
```

---

## Services

| Service                 | Role             |   Port |
| ----------------------- | ---------------- | -----: |
| `ECOM-APIGateway`       | API Gateway      | `9090` |
| `ECOM-EurekaServer`     | Service Registry | `8761` |
| `ECOM-InventoryService` | Producer Service | `8081` |
| `ECOM-OrderService`     | Consumer Service | `8082` |

---

## Project Structure

```text
ECOM-Microservices/
│
├── ECOM-APIGateway/
│   └── API Gateway
│
├── ECOM-EurekaServer/
│   └── Netflix Eureka Server
│
├── ECOM-InventoryService/
│   └── Producer Service
│
└── ECOM-OrderService/
    └── Consumer Service
```

---

## Technology Stack

* Java 21
* Spring Boot
* Spring Cloud
* Spring Cloud Gateway
* Netflix Eureka
* Spring Cloud OpenFeign
* Spring Data JPA
* H2 Database
* Maven
* REST APIs

---

# 1. Eureka Server

`ECOM-EurekaServer` acts as the **Service Registry** for the microservices architecture.

It runs on:

```text
http://localhost:8761
```

Both the Inventory Service and Order Service register themselves with Eureka.

```text
                Eureka Server
                  :8761
                    ▲
          ┌─────────┴─────────┐
          │                   │
          │ Registration      │ Registration
          │                   │
          ▼                   ▼
 Inventory Service       Order Service
     :8081                   :8082
```

Eureka is currently used for **service discovery**. Load balancing through the API Gateway is planned as a future enhancement.

---

# 2. Inventory Service

`ECOM-InventoryService` is the **Producer Service**.

```text
Port: 8081
Application Name: ECOM-InventoryService
Database: H2
```

It exposes inventory-related APIs consumed by other microservices.

Example endpoint:

```text
GET /api/inventory-service/{itemId}
```

Example:

```text
GET http://localhost:8081/api/inventory-service/1
```

The service uses an in-memory H2 database for storing inventory-related data.

---

# 3. Order Service

`ECOM-OrderService` is the **Consumer Service**.

```text
Port: 8082
Application Name: ECOM-OrderService
```

The Order Service communicates with the Inventory Service using **Spring Cloud OpenFeign**.

Example controller endpoint:

```text
GET /api/order-service/{itemId}
```

Example:

```text
GET http://localhost:8082/api/order-service/1
```

---

# 4. Inter-Service Communication

The project demonstrates synchronous communication between microservices using **OpenFeign**.

The Order Service acts as the consumer and calls the Inventory Service.

```text
Order Service
   :8082
     │
     │ OpenFeign
     ▼
Eureka Service Registry
     │
     │ Service Discovery
     ▼
Inventory Service
   :8081
```

The Feign client is configured using the Inventory Service's registered service name:

```java
@FeignClient(
        name = "ECOM-InventoryService",
        configuration = InventoryClientConfiguration.class
)
public interface InventoryClient {

    @GetMapping("/api/inventory-service/{itemId}")
    ItemDTO getItemById(
            @PathVariable("itemId") Integer itemId
    );
}
```

The important point is that the Order Service does not need to hardcode the Inventory Service's host and port in the Feign client.

Instead:

```text
ECOM-InventoryService
```

is used as the logical service name.

Eureka helps the application discover the actual service instance.

---

# 5. API Gateway

`ECOM-APIGateway` acts as the **single entry point for client requests**.

```text
Port: 9090
```

Currently, the API Gateway uses **static routing**.

Example route:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: ECOM-OrderService
              uri: http://localhost:8082
              predicates:
                - Path=/api/order-service/**
```

This means requests matching:

```text
/api/order-service/**
```

are forwarded to:

```text
http://localhost:8082
```

### Example

Instead of directly calling:

```text
http://localhost:8082/api/order-service/1
```

the client can call:

```text
http://localhost:9090/api/order-service/1
```

The request flow is:

```text
Client
  │
  │ GET /api/order-service/1
  ▼
API Gateway :9090
  │
  │ Path Predicate Match
  ▼
Order Service :8082
  │
  │ OpenFeign
  ▼
Inventory Service :8081
```

### Important

The Gateway predicate is used **inside the API Gateway** to determine which route should handle the request.

The predicate itself is not sent to the downstream service.

For example:

```yaml
predicates:
  - Path=/api/order-service/**
```

matches:

```text
/api/order-service/1
```

and Gateway forwards the request to:

```text
http://localhost:8082/api/order-service/1
```

---

# 6. Complete Request Flow

A complete client request can flow through the architecture as follows:

```text
                         CLIENT
                           │
                           │
                           │ GET
                           │ /api/order-service/1
                           ▼
                   ┌─────────────────┐
                   │   API Gateway   │
                   │      :9090      │
                   └────────┬────────┘
                            │
                            │ Static Route
                            ▼
                   ┌─────────────────┐
                   │  Order Service  │
                   │      :8082      │
                   │    Consumer     │
                   └────────┬────────┘
                            │
                            │ OpenFeign
                            ▼
                   ┌─────────────────┐
                   │Inventory Service│
                   │      :8081      │
                   │    Producer     │
                   └─────────────────┘

                            ▲
                            │
                     Service Discovery
                            │
                   ┌────────┴────────┐
                   │                 │
                   │    Eureka       │
                   │     :8761       │
                   │                 │
                   └─────────────────┘
```

---

# 7. How to Run the Project

Start the services in the following order.

### Step 1 — Start Eureka Server

Run:

```text
ECOM-EurekaServer
```

Eureka will be available at:

```text
http://localhost:8761
```

---

### Step 2 — Start Inventory Service

Run:

```text
ECOM-InventoryService
```

Port:

```text
8081
```

Verify that it registers with Eureka.

---

### Step 3 — Start Order Service

Run:

```text
ECOM-OrderService
```

Port:

```text
8082
```

Verify that it registers with Eureka.

---

### Step 4 — Start API Gateway

Run:

```text
ECOM-APIGateway
```

Port:

```text
9090
```

---

# 8. Testing

## Test Inventory Service Directly

```http
GET http://localhost:8081/api/inventory-service/1
```

---

## Test Order Service Directly

```http
GET http://localhost:8082/api/order-service/1
```

---

## Test Through API Gateway

```http
GET http://localhost:9090/api/order-service/1
```

The final request should follow:

```text
Client
  ↓
API Gateway :9090
  ↓
Order Service :8082
  ↓
OpenFeign
  ↓
Inventory Service :8081
```

---

# 9. Service Discovery

Both microservices register themselves with Eureka:

```yaml
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

The service names are:

```text
ECOM-InventoryService
ECOM-OrderService
```

These logical names allow services to discover each other without relying on hardcoded service locations for inter-service communication.

---

# 10. Current Architecture vs Future Architecture

### Current API Gateway

The Gateway currently uses **static routing**:

```text
API Gateway
     │
     │ http://localhost:8082
     ▼
Order Service
```

This is intentionally implemented first to understand the fundamentals of API Gateway routing and predicates.

### Planned Architecture

The next step is to integrate the Gateway with Eureka and Spring Cloud LoadBalancer:

```text
API Gateway
     │
     │ lb://ECOM-OrderService
     ▼
Eureka
     │
     ├── Order Service :8082
     ├── Order Service :8083
     └── ...
```

This will allow multiple instances of the same service to participate in load balancing.

---

# 11. Key Concepts Demonstrated

This project focuses on understanding the following microservices concepts:

* Microservice architecture
* Service Registry
* Service Discovery
* Netflix Eureka
* API Gateway
* Gateway Routes
* Gateway Path Predicates
* Static Routing
* Inter-Service Communication
* Spring Cloud OpenFeign
* Producer and Consumer services
* REST API communication
* H2 Database
* Distributed service architecture

---

# 12. Future Improvements

The project will be extended with:

* API Gateway + Eureka service discovery
* Client-side load balancing
* Multiple instances of the same microservice
* Dynamic routing using `lb://`
* Circuit Breaker
* Retry and Timeout mechanisms
* Rate Limiting
* Centralized configuration
* Distributed tracing
* Centralized logging
* Dockerization
* Container orchestration with Kubernetes

---

## Learning Objective

The primary goal of this project is to understand **how independent Spring Boot microservices communicate with each other and how an API Gateway can act as the entry point for client requests**.

The architecture is intentionally being developed incrementally:

```text
Service Creation
      ↓
Eureka Service Discovery
      ↓
OpenFeign Inter-Service Communication
      ↓
API Gateway
      ↓
Static Gateway Routing
      ↓
Load Balancing
      ↓
Resilience & Advanced Microservices Patterns
```

This approach makes it easier to understand each component independently before combining them into a complete production-style microservices architecture.
