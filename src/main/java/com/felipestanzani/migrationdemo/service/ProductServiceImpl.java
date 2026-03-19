package com.felipestanzani.migrationdemo.service;

import com.felipestanzani.migrationdemo.dto.ProductRequest;
import com.felipestanzani.migrationdemo.dto.ProductResponse;
import com.felipestanzani.migrationdemo.exception.ProductNotFoundException;
import com.felipestanzani.migrationdemo.model.Product;
import com.felipestanzani.migrationdemo.repository.ProductRepository;
import com.felipestanzani.migrationdemo.service.interfaces.ProductService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;

    @Transactional
    public Product save(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());
        return repository.save(product);
    }

    @Override
    public List<String> findAllNames() {
        log.info("Recovering names from database");

        return repository.findAll()
                .stream()
                .map(Product::getName)
                .toList();
    }

    @Override
    public List<ProductResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(p -> new ProductResponse(p.getName(), p.getPrice()))
                .toList();
    }

    @Override
    public Product findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
