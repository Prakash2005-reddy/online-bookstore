package com.chapterco.bookstore.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = @Index(name = "idx_orders_user", columnList = "user_id"))
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_user"))
    private AppUser user;

    @Column(nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, length = 120)
    private String recipientName;

    @Column(nullable = false, length = 240)
    private String addressLine;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 24)
    private String phone;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {}

    public CustomerOrder(AppUser user, String recipientName, String addressLine, String city,
                         String postalCode, String phone, BigDecimal total) {
        this.user = user;
        this.createdAt = Instant.now();
        this.status = OrderStatus.PENDING;
        this.recipientName = recipientName;
        this.addressLine = addressLine;
        this.city = city;
        this.postalCode = postalCode;
        this.phone = phone;
        this.total = total;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public Instant getCreatedAt() { return createdAt; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getRecipientName() { return recipientName; }
    public String getAddressLine() { return addressLine; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
    public String getPhone() { return phone; }
    public BigDecimal getTotal() { return total; }
    public List<OrderItem> getItems() { return items; }
}
