package com.chapterco.bookstore.api;

import com.chapterco.bookstore.dto.CheckoutRequest;
import com.chapterco.bookstore.dto.OrderResponse;
import com.chapterco.bookstore.service.AccountService;
import com.chapterco.bookstore.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orders;
    private final AccountService accounts;

    public OrderController(OrderService orders, AccountService accounts) {
        this.orders = orders;
        this.accounts = accounts;
    }

    @PostMapping
    public OrderResponse placeOrder(@AuthenticationPrincipal UserDetails principal,
                                    @Valid @RequestBody CheckoutRequest request) {
        return orders.placeOrder(accounts.requireUser(principal), request);
    }

    @GetMapping
    public List<OrderResponse> myOrders(@AuthenticationPrincipal UserDetails principal) {
        return orders.forUser(accounts.requireUser(principal));
    }
}
