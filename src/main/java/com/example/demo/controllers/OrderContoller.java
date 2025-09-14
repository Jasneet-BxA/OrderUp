package com.example.demo.controllers;

import com.example.demo.dtos.OrderRequest;
import com.example.demo.entity.OrderEntity;
import com.example.demo.services.OrderService;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderContoller {

    private final OrderService orderService;

    @Autowired
    public OrderContoller(OrderService orderService){
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderEntity> getOrders(){
        return orderService.getOrders();
    }

    @PostMapping
    public ResponseEntity<OrderEntity> placeOrder(@RequestBody OrderRequest orderRequest){
        OrderEntity savedOrder = orderService.placeOrder(orderRequest);
        return ResponseEntity.ok(savedOrder);
    }
}
