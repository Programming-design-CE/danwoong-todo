package com.danwoog.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ShopItem item;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false, updatable = false)
    private LocalDateTime purchasedAt;

    @Builder
    public PurchaseHistory(User user, ShopItem item, Integer price) {
        this.user = user;
        this.item = item;
        this.price = price;
    }

    @PrePersist
    protected void onCreate() { purchasedAt = LocalDateTime.now(); }
}
