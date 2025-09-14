package com.example.demo.services;
import com.example.demo.dtos.OrderRequest;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.ProductEntity;
import com.example.demo.exceptions.OutOfStockException;
import com.example.demo.exceptions.ProductNotFoundException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;

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
    void setUp(){
        MockitoAnnotations.openMocks(this);
        product = new ProductEntity();
        product.setProductId(6);
        product.setProductName("Test Product");
        product.setStock(5);
    }

    @Test
    void placeOrder_success(){
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
    void placeOrder_productNotFound(){
        OrderRequest request = new OrderRequest();
        request.setProductId(999);
        request.setCustomerName("Bob");

        when(productRepository.findByIdForUpdate(999)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            orderService.placeOrder(request);
        });
    }

    @Test
    void placeOrder_outOfStock(){
        product.setStock(0);
        OrderRequest request = new OrderRequest();
        request.setProductId(6);
        request.setCustomerName("Mock User2");

        when(productRepository.findByIdForUpdate(6)).thenReturn(Optional.of(product));

        assertThrows(OutOfStockException.class, ()-> {
            orderService.placeOrder(request);
        });
    }

}
