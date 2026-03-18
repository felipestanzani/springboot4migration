package com.felipestanzani.migrationdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

public record ProductRequest(@NotBlank @Length(min = 3) String name,
                             @NotNull @Positive Double price) {
}
