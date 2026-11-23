package com.chapterco.bookstore.api;

import com.chapterco.bookstore.dto.NewsletterRequest;
import com.chapterco.bookstore.service.NewsletterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {
    private final NewsletterService newsletter;

    public NewsletterController(NewsletterService newsletter) {
        this.newsletter = newsletter;
    }

    @PostMapping
    public ResponseEntity<Void> subscribe(@Valid @RequestBody NewsletterRequest request) {
        newsletter.subscribe(request);
        return ResponseEntity.noContent().build();
    }
}
