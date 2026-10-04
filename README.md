# ECOM Microservices Architecture (Dynamic Routing)

A hands-on **Spring Boot Microservices** project demonstrating **Service Discovery (Netflix Eureka), Inter-Service Communication using OpenFeign, and Dynamic Routing with Client-Side Load Balancing using Spring Cloud Gateway**.

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
                         │ Dynamic Routing  │
                         │ (Load Balanced)  │
                         └────────┬─────────┘
                                  │
                                  │ Dynamic Route (lb://)
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
                              ▲       ▲       ▲
                              │       │       │
                         Registration / Discovery
                              │       │       │
                         ┌────┘       │       └────┐
                         │            │            │
                   API Gateway  Order Service  Inventory Service
                      :9090        :8082          :8081
```

---

## Services

| Service                 | Role                          |   Port |
| ----------------------- | ----------------------------- | -----: |
| `ECOM-EurekaServer`     | Service Registry              | `8761` |
| `ECOM-APIGateway`       | API Gateway (Dynamic Routing) | `9090` |
| `ECOM-InventoryService` | Producer Service              | `8081` |
| `ECOM-OrderService`     | Consumer Service              | `8082` |

---

## Project Structure

```text
ECOM-Microservices/
│
├── ECOM-EurekaServer/
│   └── Netflix Eureka Server (Service Registry)
│
├── ECOM-APIGateway/
│   └── Spring Cloud API Gateway (Dynamic Routing with Eureka & LoadBalancer)
│
├── ECOM-InventoryService/
│   └── Producer Service (H2 Database)
│
└── ECOM-OrderService/
    └── Consumer Service (OpenFeign Client)
```

---

## Technology Stack

* Java 21
* Spring Boot
* Spring Cloud
* Spring Cloud Gateway (WebFlux)
* Spring Cloud LoadBalancer
* Netflix Eureka Server & Client
* Spring Cloud OpenFeign
* Spring Data JPA
* H2 Database
* Maven
* REST APIs

---

# 1. Eureka Server

`ECOM-EurekaServer` acts as the centralized **Service Registry** for the microservices architecture.

It runs on:

```text
http://localhost:8761
```

All microservices—including the API Gateway, Inventory Service, and Order Service—register themselves with Eureka and fetch the registry:

```text
                       Eureka Server
                          :8761
                            ▲
          ┌─────────────────┼─────────────────┐
          │ Registration    │ Registration    │ Registration
          │ & Discovery     │ & Discovery     │ & Discovery
          ▼                 ▼                 ▼
     API Gateway     Inventory Service   Order Service
        :9090             :8081              :8082
```

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

The Feign client is configured using the Inventory Service's registered logical service name:

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

Order Service does not hardcode the host and port of the Inventory Service. Eureka resolves the logical name to actual live instances dynamically.

---

# 5. API Gateway with Dynamic Routing

`ECOM-APIGateway` acts as the **single entry point for client requests**.

```text
Port: 9090
Application Name: ECOM-APIGateway
```

Instead of hardcoded URLs (`http://localhost:8082`), the API Gateway uses **Dynamic Routing** via the `lb://` protocol with **Spring Cloud LoadBalancer** and **Eureka Service Discovery**.

### Route Configuration:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: ECOM-OrderService
              uri: lb://ECOM-OrderService
              predicates:
                - Path=/api/order-service/**
            - id: ECOM-InventoryService
              uri: lb://ECOM-InventoryService
              predicates:
                - Path=/api/inventory-service/**
```

### How Dynamic Routing Works:
1. When a request matches the predicate path (e.g. `/api/order-service/**`), Gateway looks up the service name `ECOM-OrderService` from the Eureka registry.
2. Spring Cloud LoadBalancer chooses an available instance of the service.
3. The request is dynamically forwarded to the resolved host and port.

### Example Request Flow

Instead of calling services directly:

```text
http://localhost:9090/api/order-service/1
```

Flow:

```text
Client
  │
  │ GET /api/order-service/1
  ▼
API Gateway :9090
  │
  │ Dynamic Route Resolution (lb://ECOM-OrderService)
  ▼
Order Service :8082
  │
  │ OpenFeign (ECOM-InventoryService)
  ▼
Inventory Service :8081
```

---

# 6. Complete Request Flow

A complete client request flows through the dynamic architecture as follows:

```text
                         CLIENT
                           │
                           │ GET /api/order-service/1
                           ▼
                   ┌─────────────────┐
                   │   API Gateway   │
                   │      :9090      │
                   └────────┬────────┘
                            │
                            │ Dynamic Route (lb://ECOM-OrderService)
                            ▼
                   ┌─────────────────┐
                   │  Order Service  │
                   │      :8082      │
                   │    Consumer     │
                   └────────┬────────┘
                            │
                            │ OpenFeign (lb://ECOM-InventoryService)
                            ▼
                   ┌─────────────────┐
                   │Inventory Service│
                   │      :8081      │
                   │    Producer     │
                   └─────────────────┘

                            ▲
                            │ Service Discovery
                   ┌────────┴────────┐
                   │  Eureka Server  │
                   │      :8761      │
                   └─────────────────┘
```

---

# 7. How to Run the Project

Start the services in the following order:

### Step 1 — Start Eureka Server

Run:

```text
ECOM-EurekaServer
```

Eureka Dashboard will be available at:

```text
http://localhost:8761
```

---

### Step 2 — Start Inventory Service

Run:

```text
ECOM-InventoryService
```

Port: `8081`

Verify on the Eureka Dashboard that `ECOM-INVENTORYSERVICE` is registered.

---

### Step 3 — Start Order Service

Run:

```text
ECOM-OrderService
```

Port: `8082`

Verify on the Eureka Dashboard that `ECOM-ORDERSERVICE` is registered.

---

### Step 4 — Start API Gateway

Run:

```text
ECOM-APIGateway
```

Port: `9090`

Verify on the Eureka Dashboard that `ECOM-APIGATEWAY` is registered.

---

# 8. Testing

## 1. Test Inventory Service Directly

```http
GET http://localhost:8081/api/inventory-service/1
```

---

## 2. Test Order Service Directly

```http
GET http://localhost:8082/api/order-service/1
```

---

## 3. Test Through API Gateway (Dynamic Routing)

Test Order Service via Gateway:

```http
GET http://localhost:9090/api/order-service/1
```

Test Inventory Service via Gateway:

```http
GET http://localhost:9090/api/inventory-service/1
```

The request flow through Gateway:

```text
Client
  ↓
API Gateway :9090
  ↓  (lb://ECOM-OrderService)
Order Service :8082
  ↓  (OpenFeign)
Inventory Service :8081
```

---

# 9. Service Discovery Configuration

All services register with Eureka:

```yaml
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

Registered logical names in Eureka:
* `ECOM-APIGATEWAY`
* `ECOM-INVENTORYSERVICE`
* `ECOM-ORDERSERVICE`

---

# 10. Static Routing vs Dynamic Routing

| Feature | Static Routing | Dynamic Routing (This Project) |
|---|---|---|
| Gateway URI Scheme | `http://localhost:8082` | `lb://ECOM-OrderService` |
| Service Discovery | Gateway bypasses Eureka | Gateway discovers instances via Eureka |
| Scaling | Cannot balance across multiple instances | Automatically distributes load across instances |
| Coupling | Hardcoded host & port | Loosely coupled by service names |

---

# 11. Key Concepts Demonstrated

* Microservices Architecture
* Service Registry & Discovery (Netflix Eureka)
* API Gateway Pattern (Spring Cloud Gateway)
* Dynamic Routing using `lb://` protocol
* Client-Side Load Balancing (Spring Cloud LoadBalancer)
* Inter-Service Communication (Spring Cloud OpenFeign)
* Gateway Path Predicates
* In-Memory Database (H2) & JPA Persistence

---

# 12. Future Improvements

* Multiple active instances of microservices on random ports to observe load balancing in action
* Circuit Breaker & Resilience patterns (Resilience4j)
* Centralized Configuration (Spring Cloud Config Server)
* Distributed Tracing (Micrometer & Zipkin)
* Centralized Logging (ELK Stack)
* Docker containerization & orchestration (Docker Compose / Kubernetes)