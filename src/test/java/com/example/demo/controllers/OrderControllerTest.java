package com.example.demo.controllers;

import com.example.demo.dtos.OrderRequest;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.ProductEntity;
import com.example.demo.exceptions.GlobalExceptionHandler;
import com.example.demo.exceptions.OutOfStockException;
import com.example.demo.exceptions.ProductNotFoundException;
import com.example.demo.services.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderContoller orderController;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ProductEntity makeProduct() {
        ProductEntity p = new ProductEntity();
        p.setProductId(6);
        p.setProductName("Test Product");
        p.setStock(5);
        return p;
    }

    private OrderEntity makeOrder(String customerName) {
        OrderEntity order = new OrderEntity();
        order.setOrderId(1);
        order.setCustomerName(customerName);
        order.setProduct(makeProduct());
        return order;
    }

    @Test
    void getOrders_success() throws Exception {
        OrderEntity o1 = makeOrder("Alice");
        OrderEntity o2 = makeOrder("Bob");

        when(orderService.getOrders()).thenReturn(java.util.Arrays.asList(o1, o2));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].customerName").value("Alice"))
                .andExpect(jsonPath("$[1].customerName").value("Bob"));

        verify(orderService, times(1)).getOrders();
    }

    @Test
    void placeOrder_success() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setProductId(6);
        request.setCustomerName("Charlie");

        OrderEntity saved = makeOrder("Charlie");
        saved.setOrderId(100);

        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.customerName").value("Charlie"));

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_productNotFound() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setProductId(999);
        request.setCustomerName("Ghost");

        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_outOfStock() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setProductId(6);
        request.setCustomerName("NoStock");

        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenThrow(new OutOfStockException("Product is out of stock"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product is out of stock"));

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_unexpectedError() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setProductId(6);
        request.setCustomerName("CrashUser");

        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenThrow(new RuntimeException("Database down"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class));
    }
}
