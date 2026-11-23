package com.chapterco.bookstore.dto;

import com.chapterco.bookstore.model.Category;

public record CategoryResponse(Long id, String name, String slug) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug());
    }
}
