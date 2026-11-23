package com.chapterco.bookstore.dto;

import com.chapterco.bookstore.model.Book;
import java.math.BigDecimal;

public record BookResponse(Long id, String title, String author, String description, String isbn,
                           String imageUrl, BigDecimal price, BigDecimal rating, int stock,
                           CategoryResponse category) {
    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getDescription(),
                book.getIsbn(), book.getImageUrl(), book.getPrice(), book.getRating(), book.getStock(),
                CategoryResponse.from(book.getCategory()));
    }
}
