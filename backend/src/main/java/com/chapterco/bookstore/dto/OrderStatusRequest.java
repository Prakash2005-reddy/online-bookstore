package com.chapterco.bookstore.dto;

import com.chapterco.bookstore.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(@NotNull OrderStatus status) {}
