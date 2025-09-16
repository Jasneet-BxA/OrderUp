# Spring Boot Order Processing System

## Project Structure Summary

- **Entities:** `OrderEntity`, `ProductEntity`
- **DTOs:** `OrderRequest`, `ProductRequest`(input data)
- **Repositories:** `OrderRepository`, `ProductRepository` (with pessimistic locking)
- **Service:** `OrderService`, `ProductService` (business logic, transactional)
- **Controller:** `OrderContoller`, `ProductController` (exposes REST API)
- **AOP Aspect:** `OrderProcessingAspect` (logging)
- **Exception Handling:** `GlobalExceptionHandler` for custom exceptions and generic errors

---

## Design Decisions

### Thread Safety Mechanisms

**1. `synchronized` on `ProductEntity.reserveStock()`**

- The `reserveStock()` method is marked `synchronized` to ensure that only one thread at a time can decrement the stock for a specific product instance in memory.
- This prevents race conditions where two threads might simultaneously check and decrement stock, causing an oversell.
- This locking works within the JVM scope, providing basic thread-safety at the object level.

**2. Database-Level Pessimistic Locking**

- To handle concurrent access at the database level and across distributed instances, the repository method `findByIdForUpdate()` uses **pessimistic write locking** (`LockModeType.PESSIMISTIC_WRITE`).
- This locks the product row during the transaction, preventing other transactions from reading/modifying the same product until the lock is released.
- This mechanism is critical to prevent stock inconsistencies in multi-threaded and multi-node deployments.

**3. Transactional Integrity**

- The `placeOrder()` service method is annotated with `@Transactional`, which wraps the entire operation (stock checking, decrement, order creation) into a single atomic transaction.
- If any step fails (e.g., product not found, out of stock), the transaction rolls back, ensuring data consistency.

---

### Aspect-Oriented Programming (AOP)

- The `OrderProcessingAspect` uses an `@Around` advice on the `placeOrder()` method to log:
    - Method start with input arguments
    - Method execution duration
    - Any exceptions thrown
- This cleanly separates logging from business logic, enhancing maintainability and observability without cluttering service code.

---

## API Usage & Testing Instructions

### 1. Place a Successful Order

- HTTP 200 with created order details JSON.

```bash
curl -X POST http://localhost:8080/orders \
-H "Content-Type: application/json" \
-d '{"productId": 1, "customerName": "Alice"}'
```

### 2. Trigger Product Not Found Error

- HTTP 404 with error JSON indicating "Product not found".

```bash
curl -X POST http://localhost:8080/orders \
-H "Content-Type: application/json" \
-d '{"productId": 9999, "customerName": "Bob"}'
```

### 3. Trigger Out of Stock Error

- HTTP 400 with error JSON indicating "Out of stock".

```bash
curl -X POST http://localhost:8080/orders \
-H "Content-Type: application/json" \
-d '{"productId": 2, "customerName": "Charlie"}'
```

### 4. Get all orders

- HTTP 200 with JSON array of all orders.

```bash
curl http://localhost:8080/orders
```

---

## Unit Tests & Code Coverage

### 1. GlobalExceptionHandlerTest

- **Test Cases:**
    - `handleProductNotFound()` verifies that a `ProductNotFoundException` results in HTTP 404 with the correct message and request path.
    - `handleOutOfStock()` checks that `OutOfStockException` returns HTTP 400 with proper error details.
    - `handleUnexpected()` ensures unexpected exceptions produce HTTP 500 with a generic error message.

### 2. OrderServiceTest

- **Test Cases:**
    - `placeOrder_success()` tests ordering a product with sufficient stock, checking stock decrement, order saving, and returned data correctness.
    - `placeOrder_productNotFound()` ensures a `ProductNotFoundException` is thrown when the product does not exist.
    - `placeOrder_outOfStock()` verifies that attempting to order a product with zero stock throws an `OutOfStockException`.
    - `getOrders_returnsList()` Verifies that all orders are retrieved correctly from the repository.
    - `placeOrder_multipleOrders_sequence()` Places 5 sequential orders and validates stock decrement to zero.
    - `placeOrder_negativeStock_shouldFail()` Prevents orders on products with negative stock.
    - `placeOrder_reserveStockFailsInternally()` Simulates failure in `reserveStock()` method and verifies exception.
    - `placeOrder_concurrentOrders_onlyOneSucceeds()` Simulates two concurrent orders on one stock item; confirms only one succeeds (thread-safe + DB locking).
