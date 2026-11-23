package com.chapterco.bookstore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items",
       uniqueConstraints = @UniqueConstraint(name = "uk_cart_user_book", columnNames = {"user_id", "book_id"}),
       indexes = @Index(name = "idx_cart_user", columnList = "user_id"))
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_user"))
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_book"))
    private Book book;

    @Column(nullable = false)
    private int quantity;

    protected CartItem() {}

    public CartItem(AppUser user, Book book, int quantity) {
        this.user = user;
        this.book = book;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public Book getBook() { return book; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
