package com.felipestanzani.migrationdemo.service.interfaces;

import com.felipestanzani.migrationdemo.dto.ProductRequest;
import com.felipestanzani.migrationdemo.dto.ProductResponse;
import com.felipestanzani.migrationdemo.model.Product;

import java.util.List;

public interface ProductService {
    Product save(ProductRequest request);

    List<String> findAllNames();

    List<ProductResponse> findAll();
}
