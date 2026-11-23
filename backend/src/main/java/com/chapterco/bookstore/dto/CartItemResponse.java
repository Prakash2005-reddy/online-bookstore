package com.chapterco.bookstore.dto;

import java.math.BigDecimal;

public record CartItemResponse(Long bookId, String title, String author, String imageUrl,
                               BigDecimal unitPrice, int quantity, int stock) {}
