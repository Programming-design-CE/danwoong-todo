package com.danwoog.todo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_inventory",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_id"}))
public class MemberInventory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ShopItem item;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MemberInventory(User user, ShopItem item, Integer quantity) {
        this.user = user;
        this.item = item;
        this.quantity = quantity != null ? quantity : 0;
    }

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public void addQuantity(int count) { this.quantity += count; }

    public void useQuantity(int count) {
        if (this.quantity < count) {
            throw new com.danwoog.todo.exception.CustomException.BusinessException("수량이 부족합니다.");
        }
        this.quantity -= count;
    }
}
