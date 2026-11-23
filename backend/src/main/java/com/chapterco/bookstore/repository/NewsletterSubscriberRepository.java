package com.chapterco.bookstore.repository;

import com.chapterco.bookstore.model.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    boolean existsByEmailIgnoreCase(String email);
}
