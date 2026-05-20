package com.danwoog.todo.repository.shop;

import com.danwoog.todo.domain.shop.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
}
