package com.chapterco.bookstore.service;

import com.chapterco.bookstore.dto.CheckoutRequest;
import com.chapterco.bookstore.dto.OrderResponse;
import com.chapterco.bookstore.exception.ApiException;
import com.chapterco.bookstore.model.*;
import com.chapterco.bookstore.repository.BookRepository;
import com.chapterco.bookstore.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final BookRepository books;
    private final CartService cartService;

    public OrderService(OrderRepository orders, BookRepository books, CartService cartService) {
        this.orders = orders;
        this.books = books;
        this.cartService = cartService;
    }

    @Transactional
    public OrderResponse placeOrder(AppUser user, CheckoutRequest request) {
        List<CartItem> cart = cartService.itemsForCheckout(user);
        if (cart.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Your cart is empty.");

        List<Long> bookIds = cart.stream().map(item -> item.getBook().getId()).distinct().sorted().toList();
        var lockedBooks = new java.util.LinkedHashMap<Long, Book>();
        for (Long bookId : bookIds) {
            Book locked = books.findForUpdateById(bookId)
                    .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "A book in your cart is no longer available."));
            lockedBooks.put(bookId, locked);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            Book book = lockedBooks.get(item.getBook().getId());
            if (book.getStock() < item.getQuantity()) {
                throw new ApiException(HttpStatus.CONFLICT, "There are not enough copies of “" + book.getTitle() + "” to complete your order.");
            }
            total = total.add(book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        CustomerOrder order = new CustomerOrder(user, request.recipientName().trim(), request.addressLine().trim(),
                request.city().trim(), request.postalCode().trim(), request.phone().trim(), total);
        for (CartItem item : cart) {
            Book book = lockedBooks.get(item.getBook().getId());
            book.setStock(book.getStock() - item.getQuantity());
            order.addItem(new OrderItem(book.getId(), book.getTitle(), item.getQuantity(), book.getPrice()));
        }
        CustomerOrder saved = orders.save(order);
        cartService.clear(user);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> forUser(AppUser user) {
        return orders.findAllForUser(user.getId()).stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> allOrders() {
        return orders.findAllForAdmin().stream().map(OrderResponse::from).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus nextStatus) {
        CustomerOrder order = orders.findWithItemsById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found."));
        if (order.getStatus() == OrderStatus.CANCELLED && nextStatus != OrderStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, "A cancelled order cannot be reopened.");
        }
        if (nextStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Book book = books.findForUpdateById(item.getBookId())
                        .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "An item in this order no longer exists."));
                book.setStock(book.getStock() + item.getQuantity());
            }
        }
        order.setStatus(nextStatus);
        return OrderResponse.from(order);
    }
}
