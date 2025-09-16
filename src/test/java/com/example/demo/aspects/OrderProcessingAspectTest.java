package com.example.demo.aspects;

import com.example.demo.dtos.OrderRequest;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.ProductEntity;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.services.OrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderProcessingAspectTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private ProductEntity product;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        productRepository.deleteAll();

        product = new ProductEntity();
        product.setProductName("AspectTestProduct");
        product.setStock(10);

        productRepository.save(product);
    }

    @Test
    void aspectLogsAreCaptured_whenPlaceOrderCalled(CapturedOutput output) {

        OrderRequest request = new OrderRequest();
        request.setProductId(product.getProductId());
        request.setCustomerName("AspectUser");

        OrderEntity order = orderService.placeOrder(request);

        assertNotNull(order);
        assertTrue(output.getOut().contains("Started processing Order"));
        assertTrue(output.getOut().contains("Order processed in"));
    }

    @Test
    void ensureAspectIsAppliedToProxy() {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(orderService);
        assertTrue(targetClass.getSimpleName().contains("OrderService"));
        assertNotEquals(OrderService.class, orderService.getClass(), "Service should be proxied due to @Aspect");
    }
}
