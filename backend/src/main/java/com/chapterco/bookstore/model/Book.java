package com.chapterco.bookstore.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "books", uniqueConstraints = @UniqueConstraint(name = "uk_books_isbn", columnNames = "isbn"))
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 120)
    private String author;

    @Column(length = 3000)
    private String description;

    @Column(length = 24)
    private String isbn;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(nullable = false)
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_books_category"))
    private Category category;

    protected Book() {}

    public Book(String title, String author, String description, String isbn, String imageUrl,
                BigDecimal price, BigDecimal rating, int stock, Category category) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.isbn = isbn;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
        this.stock = stock;
        this.category = category;
    }

    public void update(String title, String author, String description, String isbn, String imageUrl,
                       BigDecimal price, BigDecimal rating, int stock, Category category) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.isbn = isbn;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
        this.stock = stock;
        this.category = category;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public String getIsbn() { return isbn; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getRating() { return rating; }
    public int getStock() { return stock; }
    public Category getCategory() { return category; }

    public void setStock(int stock) { this.stock = stock; }
}
