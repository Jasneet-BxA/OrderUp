package com.example.demo.services;

import com.example.demo.dtos.ProductRequest;
import com.example.demo.entity.ProductEntity;
import com.example.demo.exceptions.ProductNotFoundException;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public List<ProductEntity> getAllProducts(){
        return productRepository.findAll();
    }
    public ProductEntity addProduct(ProductRequest productRequest) {
        ProductEntity product = new ProductEntity();
        product.setProductName(productRequest.getProductName());
        product.setStock(productRequest.getStock());
        return productRepository.save(product);
    }
    public ProductEntity updateStock(int productId, int quantityToAdd) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.setStock(product.getStock() + quantityToAdd);
        return productRepository.save(product);
    }

}

