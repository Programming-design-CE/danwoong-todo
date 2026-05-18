package com.danwoog.todo.domain.shop;

import com.danwoog.todo.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_history")
public class PurchaseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long purchaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ShopItem item;

    @Column(name = "price")
    private Integer price;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    protected PurchaseHistory() {
    }

    public PurchaseHistory(User user, ShopItem item, Integer price) {
        this.user = user;
        this.item = item;
        this.price = price;
        this.purchasedAt = LocalDateTime.now();
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public User getUser() {
        return user;
    }

    public ShopItem getItem() {
        return item;
    }

    public Integer getPrice() {
        return price;
    }
}