package com.chapterco.bookstore.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BookRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 120) String author,
        @Size(max = 3000) String description,
        @Size(max = 24) String isbn,
        @Size(max = 500) String imageUrl,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull @DecimalMin("0.0") @DecimalMax("5.0") @Digits(integer = 1, fraction = 1) BigDecimal rating,
        @Min(0) @Max(100000) int stock,
        @NotNull @Positive Long categoryId
) {}
