package com.chapterco.bookstore.api;

import com.chapterco.bookstore.dto.BookRequest;
import com.chapterco.bookstore.dto.BookResponse;
import com.chapterco.bookstore.dto.OrderResponse;
import com.chapterco.bookstore.dto.OrderStatusRequest;
import com.chapterco.bookstore.service.CatalogService;
import com.chapterco.bookstore.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CatalogService catalog;
    private final OrderService orders;

    public AdminController(CatalogService catalog, OrderService orders) {
        this.catalog = catalog;
        this.orders = orders;
    }

    @PostMapping("/books")
    public ResponseEntity<BookResponse> addBook(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalog.create(request));
    }

    @PutMapping("/books/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return catalog.update(id, request);
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        catalog.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public List<OrderResponse> orders() {
        return orders.allOrders();
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        return orders.updateStatus(id, request.status());
    }
}
