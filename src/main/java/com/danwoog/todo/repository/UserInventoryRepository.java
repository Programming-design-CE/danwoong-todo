package com.danwoong.repository;

import com.danwoong.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {
    List<UserInventory> findByUser(User user);
    Optional<UserInventory> findByUserAndItem(User user, ShopItem item);
}
