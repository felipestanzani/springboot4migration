package com.felipestanzani.migrationdemo.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID id) {
        super("Product not found with id: " + id);
    }
}
