package com.danwoog.todo.repository;

import com.danwoog.todo.domain.shop.UserInventory;
import com.danwoog.todo.domain.shop.ShopItem;
import com.danwoog.todo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberInventoryRepository extends JpaRepository<UserInventory, Long> {
    List<UserInventory> findByUser(User user);
    Optional<UserInventory> findByUserAndItem(User user, ShopItem item);
}
