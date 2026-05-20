package com.danwoog.todo.domain.shop;

import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.exception.CustomException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_inventory")
public class UserInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ShopItem item;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected UserInventory() {}

    public UserInventory(User user, ShopItem item, Integer quantity) {
        this.user = user;
        this.item = item;
        this.quantity = quantity != null ? quantity : 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addQuantity(int count) {
        this.quantity += count;
        this.updatedAt = LocalDateTime.now();
    }

    public void useQuantity(int count) {
        if (this.quantity < count) {
            throw new CustomException.BusinessException("수량이 부족합니다.");
        }
        this.quantity -= count;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getInventoryId() { return inventoryId; }
    public User getUser() { return user; }
    public ShopItem getItem() { return item; }
    public Integer getQuantity() { return quantity; }
}
