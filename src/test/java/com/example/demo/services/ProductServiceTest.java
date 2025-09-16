package com.example.demo.services;

import com.example.demo.dtos.ProductRequest;
import com.example.demo.entity.ProductEntity;
import com.example.demo.exceptions.ProductNotFoundException;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductEntity mockProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockProduct = new ProductEntity();
        mockProduct.setProductId(1);
        mockProduct.setProductName("Test Product");
        mockProduct.setStock(10);
    }

    @Test
    void testGetAllProducts() {
        List<ProductEntity> mockList = Arrays.asList(mockProduct);

        when(productRepository.findAll()).thenReturn(mockList);

        List<ProductEntity> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getProductName());

        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testAddProduct() {
        ProductRequest request = new ProductRequest();
        request.setProductName("New Product");
        request.setStock(5);

        ProductEntity savedProduct = new ProductEntity();
        savedProduct.setProductId(2);
        savedProduct.setProductName("New Product");
        savedProduct.setStock(5);

        when(productRepository.save(any(ProductEntity.class))).thenReturn(savedProduct);

        ProductEntity result = productService.addProduct(request);

        assertNotNull(result);
        assertEquals("New Product", result.getProductName());
        assertEquals(5, result.getStock());

        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void testUpdateStock_success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        ProductEntity updatedProduct = productService.updateStock(1, 5);

        assertNotNull(updatedProduct);
        assertEquals(15, updatedProduct.getStock());

        verify(productRepository).findById(1);
        verify(productRepository).save(mockProduct);
    }

    @Test
    void testUpdateStock_productNotFound() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.updateStock(99, 5);
        });

        verify(productRepository).findById(99);
        verify(productRepository, never()).save(any(ProductEntity.class));
    }
}
