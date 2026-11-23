package com.chapterco.bookstore.service;

import com.chapterco.bookstore.dto.RegisterRequest;
import com.chapterco.bookstore.exception.ApiException;
import com.chapterco.bookstore.model.AppUser;
import com.chapterco.bookstore.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class AccountService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with that email already exists.");
        }
        return users.save(new AppUser(request.name().trim(), email, passwordEncoder.encode(request.password()), "USER"));
    }

    @Transactional(readOnly = true)
    public AppUser requireUser(UserDetails principal) {
        return users.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found."));
    }
}
