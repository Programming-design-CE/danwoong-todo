package com.danwoog.todo.repository;

import com.danwoog.todo.domain.MemberInventory;
import com.danwoog.todo.domain.ShopItem;
import com.danwoog.todo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberInventoryRepository extends JpaRepository<MemberInventory, Long> {
    List<MemberInventory> findByUser(User user);
    Optional<MemberInventory> findByUserAndItem(User user, ShopItem item);
}
