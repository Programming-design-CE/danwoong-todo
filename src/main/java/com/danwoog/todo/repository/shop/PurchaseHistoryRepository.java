package com.danwoog.todo.repository.shop;

import com.danwoog.todo.domain.shop.PurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
}
