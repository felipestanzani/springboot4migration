package com.felipestanzani.migrationdemo.service;

import com.felipestanzani.migrationdemo.dto.ProductRequest;
import com.felipestanzani.migrationdemo.dto.ProductResponse;
import com.felipestanzani.migrationdemo.exception.ForcedFallbackException;
import com.felipestanzani.migrationdemo.exception.ProductNotFoundException;
import com.felipestanzani.migrationdemo.model.Product;
import com.felipestanzani.migrationdemo.repository.ProductRepository;
import com.felipestanzani.migrationdemo.service.interfaces.ProductService;
import io.github.resilience4j.retry.annotation.Retry;
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

    @Retry(name = "findAllNames"
            , fallbackMethod = "fallbackFindAllNames")
    @Override
    public List<String> findAllNames() {
        if (Math.random() >= 0.5) throw new ForcedFallbackException("It is not frozen, it is in panic!!!");

        log.info("Recovering names from database");

        return repository.findAll()
                .stream()
                .map(Product::getName)
                .toList();
    }

    @SuppressWarnings("Not used")
    public List<String> fallbackFindAllNames(Exception exception) {
        if (exception instanceof ForcedFallbackException fallbackException) {
            log.error("Fallback exception: {}", fallbackException.getMessage());
        }
        log.warn("Names recovered from fallback!!!");

        return List.of("Charuteira", "Infundibuliar");
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
