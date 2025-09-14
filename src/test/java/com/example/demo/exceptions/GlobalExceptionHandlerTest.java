package com.example.demo.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/test-path");
    }

    @Test
    void handleProductNotFound() {
        ProductNotFoundException ex = new ProductNotFoundException("Product not found");
        ResponseEntity<?> response = handler.handleProductNotFound(ex, mockRequest);

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Product not found"));
        assertTrue(response.getBody().toString().contains("/test-path"));
    }

    @Test
    void handleOutOfStock() {
        OutOfStockException ex = new OutOfStockException("Out of stock");
        ResponseEntity<?> response = handler.handleOutOfStock(ex, mockRequest);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Out of stock"));
        assertTrue(response.getBody().toString().contains("/test-path"));
    }

    @Test
    void handleUnexpected() {
        Exception ex = new Exception("Some error");
        ResponseEntity<?> response = handler.handleUnexpected(ex, mockRequest);

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("unexpected"));
        assertTrue(response.getBody().toString().contains("/test-path"));
    }
}
