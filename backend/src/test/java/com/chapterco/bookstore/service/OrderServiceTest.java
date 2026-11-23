package com.chapterco.bookstore.service;

import com.chapterco.bookstore.dto.CheckoutRequest;
import com.chapterco.bookstore.exception.ApiException;
import com.chapterco.bookstore.model.AppUser;
import com.chapterco.bookstore.model.Book;
import com.chapterco.bookstore.model.CartItem;
import com.chapterco.bookstore.model.CustomerOrder;
import com.chapterco.bookstore.repository.BookRepository;
import com.chapterco.bookstore.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orders;
    @Mock private BookRepository books;
    @Mock private CartService cartService;
    @InjectMocks private OrderService orderService;

    @Test
    void checkoutRejectsAnEmptyCart() {
        AppUser user = mock(AppUser.class);
        when(cartService.itemsForCheckout(user)).thenReturn(List.of());
        CheckoutRequest request = new CheckoutRequest("Reader", "1 Main Street", "Portland", "97201", "5035550100");

        ApiException error = assertThrows(ApiException.class, () -> orderService.placeOrder(user, request));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verifyNoInteractions(books, orders);
        verify(cartService, never()).clear(user);
    }

    @Test
    void checkoutDecrementsStockAndPersistsAnOrder() {
        AppUser user = mock(AppUser.class);
        Book book = mock(Book.class);
        CartItem item = mock(CartItem.class);
        when(user.getName()).thenReturn("Reader");
        when(book.getId()).thenReturn(12L);
        when(book.getTitle()).thenReturn("The Quiet Atlas");
        when(book.getPrice()).thenReturn(new BigDecimal("7.50"));
        when(book.getStock()).thenReturn(5);
        when(item.getBook()).thenReturn(book);
        when(item.getQuantity()).thenReturn(2);
        when(cartService.itemsForCheckout(user)).thenReturn(List.of(item));
        when(books.findForUpdateById(12L)).thenReturn(Optional.of(book));
        when(orders.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CheckoutRequest request = new CheckoutRequest("Reader", "1 Main Street", "Portland", "97201", "5035550100");

        var response = orderService.placeOrder(user, request);

        assertEquals(new BigDecimal("15.00"), response.total());
        assertEquals(1, response.items().size());
        verify(book).setStock(3);
        verify(orders).save(any(CustomerOrder.class));
        verify(cartService).clear(user);
    }
}
