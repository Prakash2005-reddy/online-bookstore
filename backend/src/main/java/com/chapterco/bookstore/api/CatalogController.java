package com.chapterco.bookstore.api;

import com.chapterco.bookstore.dto.BookResponse;
import com.chapterco.bookstore.dto.CategoryResponse;
import com.chapterco.bookstore.service.CatalogService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/books")
    public List<BookResponse> books(@RequestParam(required = false) String q,
                                    @RequestParam(required = false) Long categoryId) {
        return catalog.search(q, categoryId);
    }

    @GetMapping("/books/{id}")
    public BookResponse book(@PathVariable Long id) {
        return catalog.getBook(id);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return catalog.categories();
    }
}
