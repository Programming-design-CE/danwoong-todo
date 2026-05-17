package com.danwoog.todo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * user 브랜치 기준: UserCharacter 참조, equipped_id PK
 * 내 코드 추가: changeItem() 비즈니스 메서드, UNIQUE 제약, @PrePersist
 */
@Entity
@Table(name = "character_equipped_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"character_id", "slot_type"}))
public class CharacterEquippedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipped_id")
    private Long equippedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private UserCharacter character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ShopItem item;

    // HAT, CLOTHES, ACCESSORY, BACKGROUND, color
    @Column(name = "slot_type", nullable = false, length = 30)
    private String slotType;

    @Column(name = "equipped_at", nullable = false)
    private LocalDateTime equippedAt;

    protected CharacterEquippedItem() {}

    public CharacterEquippedItem(UserCharacter character, ShopItem item, String slotType) {
        this.character = character;
        this.item = item;
        this.slotType = slotType;
    }

    @PrePersist
    protected void onCreate() { equippedAt = LocalDateTime.now(); }

    public void changeItem(ShopItem newItem) {
        this.item = newItem;
        this.equippedAt = LocalDateTime.now();
    }

    public Long getEquippedId() { return equippedId; }
    public UserCharacter getCharacter() { return character; }
    public ShopItem getItem() { return item; }
    public String getSlotType() { return slotType; }
}
