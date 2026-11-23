package com.chapterco.bookstore.service;

import com.chapterco.bookstore.dto.CartAddRequest;
import com.chapterco.bookstore.dto.CartItemResponse;
import com.chapterco.bookstore.dto.CartResponse;
import com.chapterco.bookstore.dto.QuantityRequest;
import com.chapterco.bookstore.exception.ApiException;
import com.chapterco.bookstore.model.AppUser;
import com.chapterco.bookstore.model.Book;
import com.chapterco.bookstore.model.CartItem;
import com.chapterco.bookstore.repository.BookRepository;
import com.chapterco.bookstore.repository.CartItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {
    private final CartItemRepository cartItems;
    private final BookRepository books;

    public CartService(CartItemRepository cartItems, BookRepository books) {
        this.cartItems = cartItems;
        this.books = books;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(AppUser user) {
        return response(cartItems.findCartByUserId(user.getId()));
    }

    @Transactional
    public CartResponse add(AppUser user, CartAddRequest request) {
        Book book = books.findById(request.bookId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Book not found."));
        CartItem existing = cartItems.findByUserIdAndBookId(user.getId(), book.getId()).orElse(null);
        int nextQuantity = request.quantity() + (existing == null ? 0 : existing.getQuantity());
        ensureStock(book, nextQuantity);
        if (existing == null) cartItems.save(new CartItem(user, book, request.quantity()));
        else existing.setQuantity(nextQuantity);
        return getCart(user);
    }

    @Transactional
    public CartResponse update(AppUser user, Long bookId, QuantityRequest request) {
        CartItem item = cartItems.findByUserIdAndBookId(user.getId(), bookId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "That book is not in your cart."));
        ensureStock(item.getBook(), request.quantity());
        item.setQuantity(request.quantity());
        return getCart(user);
    }

    @Transactional
    public void remove(AppUser user, Long bookId) {
        if (cartItems.findByUserIdAndBookId(user.getId(), bookId).isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "That book is not in your cart.");
        }
        cartItems.deleteByUserIdAndBookId(user.getId(), bookId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> itemsForCheckout(AppUser user) {
        return cartItems.findCartByUserId(user.getId());
    }

    @Transactional
    public void clear(AppUser user) {
        cartItems.deleteAllByUserId(user.getId());
    }

    private CartResponse response(List<CartItem> items) {
        List<CartItemResponse> lines = items.stream().map(item -> new CartItemResponse(
                item.getBook().getId(), item.getBook().getTitle(), item.getBook().getAuthor(),
                item.getBook().getImageUrl(), item.getBook().getPrice(), item.getQuantity(), item.getBook().getStock()
        )).toList();
        BigDecimal total = lines.stream().map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(lines, total);
    }

    private void ensureStock(Book book, int quantity) {
        if (book.getStock() < quantity) {
            throw new ApiException(HttpStatus.CONFLICT, "Only " + book.getStock() + " copies of “" + book.getTitle() + "” are currently available.");
        }
    }
}
