package com.example.demo.services;

import com.example.demo.dtos.OrderRequest;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.ProductEntity;
import com.example.demo.exceptions.OutOfStockException;
import com.example.demo.exceptions.ProductNotFoundException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private ProductEntity product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        product = new ProductEntity();
        product.setProductId(6);
        product.setProductName("Test Product");
        product.setStock(5);
    }

    @Test
    void placeOrder_success() {
        OrderRequest request = new OrderRequest();
        request.setProductId(6);
        request.setCustomerName("Mock UserName");

        when(productRepository.findByIdForUpdate(6)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderEntity order = orderService.placeOrder(request);

        assertNotNull(order);
        assertEquals("Mock UserName", order.getCustomerName());
        assertEquals(product, order.getProduct());
        assertEquals(4, product.getStock());

        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void placeOrder_productNotFound() {
        OrderRequest request = new OrderRequest();
        request.setProductId(999);
        request.setCustomerName("Bob");

        when(productRepository.findByIdForUpdate(999)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            orderService.placeOrder(request);
        });
    }

    @Test
    void placeOrder_outOfStock() {
        product.setStock(0);
        OrderRequest request = new OrderRequest();
        request.setProductId(6);
        request.setCustomerName("Mock User2");

        when(productRepository.findByIdForUpdate(6)).thenReturn(Optional.of(product));

        assertThrows(OutOfStockException.class, () -> {
            orderService.placeOrder(request);
        });
    }

    @Test
    void getOrders_returnsList() {
        OrderEntity order1 = new OrderEntity();
        order1.setOrderId(1);
        order1.setCustomerName("A");
        order1.setProduct(product);

        OrderEntity order2 = new OrderEntity();
        order2.setOrderId(2);
        order2.setCustomerName("B");
        order2.setProduct(product);

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        List<OrderEntity> orders = orderService.getOrders();

        assertEquals(2, orders.size());
        assertEquals("A", orders.get(0).getCustomerName());
    }

    @Test
    void placeOrder_multipleOrders_sequence() {
        OrderRequest request = new OrderRequest();
        request.setProductId(6);
        request.setCustomerName("User");

        when(productRepository.findByIdForUpdate(6)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        for (int i = 0; i < 5; i++) {
            request.setCustomerName("User" + i);
            OrderEntity order = orderService.placeOrder(request);
            assertEquals("User" + i, order.getCustomerName());
        }

        assertEquals(0, product.getStock());
    }

    @Test
    void placeOrder_negativeStock_shouldFail() {
        product.setStock(-1);
        OrderRequest request = new OrderRequest();
        request.setProductId(6);
        request.setCustomerName("User");

        when(productRepository.findByIdForUpdate(6)).thenReturn(Optional.of(product));

        assertThrows(OutOfStockException.class, () -> {
            orderService.placeOrder(request);
        });
    }

    @Test
    void placeOrder_reserveStockFailsInternally() {
        ProductEntity mockProduct = spy(product);
        doReturn(false).when(mockProduct).reserveStock();

        OrderRequest request = new OrderRequest();
        request.setProductId(6);
        request.setCustomerName("User");

        when(productRepository.findByIdForUpdate(6)).thenReturn(Optional.of(mockProduct));

        assertThrows(OutOfStockException.class, () -> {
            orderService.placeOrder(request);
        });

        verify(mockProduct, times(1)).reserveStock();
    }

    @Test
    void placeOrder_concurrentOrders_onlyOneSucceeds() throws InterruptedException {
        product.setStock(1);

        when(productRepository.findByIdForUpdate(6)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderRequest request1 = new OrderRequest();
        request1.setProductId(6);
        request1.setCustomerName("User1");

        OrderRequest request2 = new OrderRequest();
        request2.setProductId(6);
        request2.setCustomerName("User2");

        CountDownLatch latch = new CountDownLatch(2);

        Runnable task1 = () -> {
            try {
                orderService.placeOrder(request1);
            } catch (Exception e) {
                System.out.println("User1 failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        };

        Runnable task2 = () -> {
            try {
                Thread.sleep(10);
                orderService.placeOrder(request2);
            } catch (Exception e) {
                System.out.println("User2 failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        };

        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();

        latch.await();

        assertEquals(0, product.getStock());
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

}
