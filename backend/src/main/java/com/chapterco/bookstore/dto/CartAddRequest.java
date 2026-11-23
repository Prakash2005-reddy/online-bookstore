package com.chapterco.bookstore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartAddRequest(@NotNull @Positive Long bookId, @Min(1) @Max(100) int quantity) {}
