package com.danwoog.todo.domain.shop;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String itemName;

    // HAT, CLOTHES, ACCESSORY, BACKGROUND, DYE
    @Column(nullable = false, length = 30)
    private String itemType;

    @Column(length = 255)
    private String itemImage;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ShopItem(String itemName, String itemType, String itemImage, Integer price) {
        this.itemName = itemName;
        this.itemType = itemType;
        this.itemImage = itemImage;
        this.price = price;
    }

    public void updateCatalog(String itemType, String itemImage, Integer price) {
        this.itemType = itemType;
        this.itemImage = itemImage;
        this.price = price;
    }

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
