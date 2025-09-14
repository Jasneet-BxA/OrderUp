package com.example.demo.services;

import com.example.demo.dtos.OrderRequest;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.ProductEntity;
import com.example.demo.exceptions.OutOfStockException;
import com.example.demo.exceptions.ProductNotFoundException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<OrderEntity> getOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public OrderEntity placeOrder(OrderRequest request) {
        ProductEntity product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        boolean reserved = product.reserveStock();
        if (!reserved) {
            throw new OutOfStockException("Product is out of stock");
        }
        productRepository.save(product);

        OrderEntity order = new OrderEntity();
        order.setCustomerName(request.getCustomerName());
        order.setProduct(product);
        OrderEntity savedOrder = orderRepository.save(order); // This instance will have the ID set
        return savedOrder;

    }
}
