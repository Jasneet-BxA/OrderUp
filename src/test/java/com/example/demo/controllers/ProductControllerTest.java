package com.example.demo.controllers;

import com.example.demo.dtos.ProductRequest;
import com.example.demo.entity.ProductEntity;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
    }

    @Test
    void testAddProduct() {
        ProductRequest request = new ProductRequest();
        request.setProductName("Sample Product");
        request.setStock(10);

        ResponseEntity<ProductEntity> response = restTemplate.postForEntity("/products", request, ProductEntity.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProductEntity body = response.getBody();
        assertNotNull(body);
        assertEquals("Sample Product", body.getProductName());
        assertEquals(10, body.getStock());
    }

    @Test
    void testGetAllProducts() {
        ProductEntity p1 = new ProductEntity();
        p1.setProductName("Test 1");
        p1.setStock(3);

        ProductEntity p2 = new ProductEntity();
        p2.setProductName("Test 2");
        p2.setStock(5);

        productRepository.saveAndFlush(p1);
        productRepository.saveAndFlush(p2);

        ResponseEntity<ProductEntity[]> response = restTemplate.getForEntity("/products", ProductEntity[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 2);
    }

    @Test
    void testUpdateProductStock_success() {
        ProductEntity existingProduct = new ProductEntity();
        existingProduct.setProductName("Sample Product");
        existingProduct.setStock(10);
        existingProduct = productRepository.saveAndFlush(existingProduct);

        String url = "/products/" + existingProduct.getProductId() + "/update?stock=5";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ProductEntity> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, ProductEntity.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProductEntity updatedProduct = response.getBody();
        assertNotNull(updatedProduct);
        assertEquals(15, updatedProduct.getStock());
    }

    @Test
    void testUpdateProductStock_productNotFound() {
        String url = "/products/99999/update?stock=5";

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Product not found"));
    }

    @Test
    void testAddMultipleProducts_withUniqueNames() {
        for (int i = 1; i <= 3; i++) {
            ProductRequest request = new ProductRequest();
            request.setProductName("Product-" + UUID.randomUUID());
            request.setStock(i * 10);

            ResponseEntity<ProductEntity> response = restTemplate.postForEntity("/products", request, ProductEntity.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        List<ProductEntity> products = productRepository.findAll();
        assertEquals(3, products.size());
    }
}
