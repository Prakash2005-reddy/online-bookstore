package com.chapterco.bookstore.service;

import com.chapterco.bookstore.dto.CartAddRequest;
import com.chapterco.bookstore.exception.ApiException;
import com.chapterco.bookstore.model.AppUser;
import com.chapterco.bookstore.model.Book;
import com.chapterco.bookstore.model.CartItem;
import com.chapterco.bookstore.repository.BookRepository;
import com.chapterco.bookstore.repository.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock private CartItemRepository cartItems;
    @Mock private BookRepository books;
    @InjectMocks private CartService cartService;

    @Test
    void addRejectsQuantityThatWouldExceedStock() {
        AppUser user = mock(AppUser.class);
        Book book = mock(Book.class);
        CartItem existingItem = mock(CartItem.class);
        when(user.getId()).thenReturn(4L);
        when(book.getId()).thenReturn(12L);
        when(book.getStock()).thenReturn(1);
        when(book.getTitle()).thenReturn("The Quiet Atlas");
        when(existingItem.getQuantity()).thenReturn(1);
        when(books.findById(12L)).thenReturn(Optional.of(book));
        when(cartItems.findByUserIdAndBookId(4L, 12L)).thenReturn(Optional.of(existingItem));

        ApiException error = assertThrows(ApiException.class,
                () -> cartService.add(user, new CartAddRequest(12L, 1)));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(cartItems, never()).save(any(CartItem.class));
        verify(existingItem, never()).setQuantity(anyInt());
    }

    @Test
    void addPersistsNewItemWhenStockIsAvailable() {
        AppUser user = mock(AppUser.class);
        Book book = mock(Book.class);
        when(user.getId()).thenReturn(4L);
        when(book.getId()).thenReturn(12L);
        when(book.getStock()).thenReturn(3);
        when(books.findById(12L)).thenReturn(Optional.of(book));
        when(cartItems.findByUserIdAndBookId(4L, 12L)).thenReturn(Optional.empty());
        when(cartItems.findCartByUserId(4L)).thenReturn(java.util.List.of());

        cartService.add(user, new CartAddRequest(12L, 2));

        verify(cartItems).save(argThat(item -> item.getUser() == user && item.getBook() == book && item.getQuantity() == 2));
    }
}
