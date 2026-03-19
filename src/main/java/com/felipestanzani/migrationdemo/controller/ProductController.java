package com.felipestanzani.migrationdemo.controller;

import com.felipestanzani.migrationdemo.dto.ProductRequest;
import com.felipestanzani.migrationdemo.dto.ProductResponse;
import com.felipestanzani.migrationdemo.model.Product;
import com.felipestanzani.migrationdemo.service.interfaces.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        var savedProduct = productService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @GetMapping(version = "v1")
    public ResponseEntity<List<String>> getProducts() {
        return ResponseEntity.ok().body(productService.findAllNames());
    }

    @GetMapping(version = "v2")
    public ResponseEntity<List<ProductResponse>> getProductsV2() {
        return ResponseEntity.ok().body(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        var product = productService.findById(id);
        return ResponseEntity.ok().body(product);
    }
}
