package com.chapterco.bookstore.service;

import com.chapterco.bookstore.dto.NewsletterRequest;
import com.chapterco.bookstore.model.NewsletterSubscriber;
import com.chapterco.bookstore.repository.NewsletterSubscriberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class NewsletterService {
    private final NewsletterSubscriberRepository subscribers;

    public NewsletterService(NewsletterSubscriberRepository subscribers) {
        this.subscribers = subscribers;
    }

    @Transactional
    public void subscribe(NewsletterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (!subscribers.existsByEmailIgnoreCase(email)) {
            subscribers.save(new NewsletterSubscriber(email));
        }
    }
}
