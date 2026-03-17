package com.felipestanzani.migrationdemo.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {
}
