package com.felipestanzani.migrationdemo.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        LocalDateTime timestamp,
        Map<String, Object> errors
) {
}
