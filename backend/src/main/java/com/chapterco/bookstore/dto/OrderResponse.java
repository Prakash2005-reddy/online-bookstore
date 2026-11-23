package com.chapterco.bookstore.dto;

import com.chapterco.bookstore.model.CustomerOrder;
import com.chapterco.bookstore.model.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, String userName, Instant createdAt, String status, BigDecimal total,
                            String recipientName, String addressLine, String city, String postalCode,
                            String phone, List<OrderItemResponse> items) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getUser().getName(), order.getCreatedAt(),
                order.getStatus().name(), order.getTotal(), order.getRecipientName(), order.getAddressLine(),
                order.getCity(), order.getPostalCode(), order.getPhone(),
                order.getItems().stream().map(OrderResponse::item).toList());
    }

    private static OrderItemResponse item(OrderItem item) {
        return new OrderItemResponse(item.getBookId(), item.getTitle(), item.getQuantity(), item.getUnitPrice());
    }
}
