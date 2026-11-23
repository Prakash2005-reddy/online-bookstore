package com.chapterco.bookstore.api;

import com.chapterco.bookstore.dto.CartAddRequest;
import com.chapterco.bookstore.dto.CartResponse;
import com.chapterco.bookstore.dto.QuantityRequest;
import com.chapterco.bookstore.service.AccountService;
import com.chapterco.bookstore.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cart;
    private final AccountService accounts;

    public CartController(CartService cart, AccountService accounts) {
        this.cart = cart;
        this.accounts = accounts;
    }

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal UserDetails principal) {
        return cart.getCart(accounts.requireUser(principal));
    }

    @PostMapping("/items")
    public CartResponse add(@AuthenticationPrincipal UserDetails principal, @Valid @RequestBody CartAddRequest request) {
        return cart.add(accounts.requireUser(principal), request);
    }

    @PutMapping("/items/{bookId}")
    public CartResponse update(@AuthenticationPrincipal UserDetails principal, @PathVariable Long bookId,
                              @Valid @RequestBody QuantityRequest request) {
        return cart.update(accounts.requireUser(principal), bookId, request);
    }

    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserDetails principal, @PathVariable Long bookId) {
        cart.remove(accounts.requireUser(principal), bookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
