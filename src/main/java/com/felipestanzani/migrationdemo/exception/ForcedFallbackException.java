package com.felipestanzani.migrationdemo.exception;

public class ForcedFallbackException extends RuntimeException {
    public ForcedFallbackException(String message) {
        super(message);
    }
}
