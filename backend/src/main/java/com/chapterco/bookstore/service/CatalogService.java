package com.chapterco.bookstore.service;

import com.chapterco.bookstore.dto.BookRequest;
import com.chapterco.bookstore.dto.BookResponse;
import com.chapterco.bookstore.dto.CategoryResponse;
import com.chapterco.bookstore.exception.ApiException;
import com.chapterco.bookstore.model.Book;
import com.chapterco.bookstore.model.Category;
import com.chapterco.bookstore.repository.BookRepository;
import com.chapterco.bookstore.repository.CartItemRepository;
import com.chapterco.bookstore.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CatalogService {
    private final BookRepository books;
    private final CategoryRepository categories;
    private final CartItemRepository cartItems;

    public CatalogService(BookRepository books, CategoryRepository categories, CartItemRepository cartItems) {
        this.books = books;
        this.categories = categories;
        this.cartItems = cartItems;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> search(String query, Long categoryId) {
        if (categoryId != null && !categories.existsById(categoryId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Category not found.");
        }
        String normalized = query == null || query.isBlank() ? null : query.trim();
        return books.search(normalized, categoryId).stream().map(BookResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse getBook(Long id) {
        return BookResponse.from(requireBook(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories() {
        return categories.findAll().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(CategoryResponse::from).toList();
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        Category category = requireCategory(request.categoryId());
        Book book = new Book(request.title().trim(), request.author().trim(), clean(request.description()),
                clean(request.isbn()), clean(request.imageUrl()), request.price(), request.rating(), request.stock(), category);
        return BookResponse.from(books.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = requireBook(id);
        Category category = requireCategory(request.categoryId());
        book.update(request.title().trim(), request.author().trim(), clean(request.description()),
                clean(request.isbn()), clean(request.imageUrl()), request.price(), request.rating(), request.stock(), category);
        return BookResponse.from(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = requireBook(id);
        if (cartItems.existsByBookId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "This book is currently in a customer cart and cannot be deleted.");
        }
        books.delete(book);
    }

    private Book requireBook(Long id) {
        return books.findWithCategoryById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Book not found."));
    }

    private Category requireCategory(Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found."));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
