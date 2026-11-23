package com.chapterco.bookstore.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "newsletter_subscribers", uniqueConstraints = @UniqueConstraint(name = "uk_newsletter_email", columnNames = "email"))
public class NewsletterSubscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 190)
    private String email;

    @Column(nullable = false)
    private Instant subscribedAt;

    protected NewsletterSubscriber() {}

    public NewsletterSubscriber(String email) {
        this.email = email;
        this.subscribedAt = Instant.now();
    }
}
