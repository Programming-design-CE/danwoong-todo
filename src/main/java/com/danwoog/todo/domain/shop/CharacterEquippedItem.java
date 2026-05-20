package com.danwoog.todo.domain.shop;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "character_equipped_items")
public class CharacterEquippedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipped_id")
    private Long equippedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private UserCharacter character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private ShopItem item;

    @Column(name = "slot_type")
    private String slotType;

    @Column(name = "equipped_at")
    private LocalDateTime equippedAt;

    protected CharacterEquippedItem() {
    }

    public Long getEquippedId() {
        return equippedId;
    }

    public UserCharacter getCharacter() {
        return character;
    }

    public ShopItem getItem() {
        return item;
    }

    public String getSlotType() {
        return slotType;
    }

    public CharacterEquippedItem(UserCharacter character, ShopItem item, String slotType) {
        this.character = character;
        this.item = item;
        this.slotType = slotType;
        this.equippedAt = LocalDateTime.now();
    }

    public void changeItem(ShopItem newItem) {
        this.item = newItem;
        this.equippedAt = LocalDateTime.now();
    }
}