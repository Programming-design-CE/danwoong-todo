package com.danwoog.todo.repository;

import com.danwoog.todo.domain.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
}
