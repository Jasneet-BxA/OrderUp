package com.example.demo.controllers;

import com.example.demo.dtos.ProductRequest;
import com.example.demo.entity.ProductEntity;
import com.example.demo.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping
    public List<ProductEntity> getAllProducts(){
        return productService.getAllProducts();
    }
    @PostMapping
    public ResponseEntity<ProductEntity> addProduct(@RequestBody ProductRequest productRequest){
        ProductEntity savedProduct = productService.addProduct(productRequest);
        return ResponseEntity.ok(savedProduct);
    }
    @PatchMapping("/{id}/update")
    public ResponseEntity<ProductEntity> updateProductStock(
            @PathVariable int id,
            @RequestParam int stock) {

        ProductEntity updatedProduct = productService.updateStock(id, stock);
        return ResponseEntity.ok(updatedProduct);
    }
}
