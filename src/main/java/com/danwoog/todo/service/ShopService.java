package com.danwoog.todo.service;

import com.danwoog.todo.domain.shop.ShopItem;
import com.danwoog.todo.domain.shop.UserInventory;
import com.danwoog.todo.domain.shop.PurchaseHistory;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.shop.ShopDto.*;
import com.danwoog.todo.exception.CustomException.*;
import com.danwoog.todo.repository.shop.*;
import com.danwoog.todo.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopService {

    private final ShopItemRepository shopItemRepository;
    private final MemberInventoryRepository memberInventoryRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final UserRepository userRepository;

    public GarlicResponse getGarlic(Long userId) {
        User user = findUser(userId);
        return new GarlicResponse(user.getGarlicCount());
    }

    public List<ShopItemResponse> getShopItems() {
        return shopItemRepository.findAll().stream()
                .map(this::toShopItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PurchaseResponse purchaseItem(Long userId, Long itemId, PurchaseRequest request) {
        User user = findUser(userId);
        ShopItem shopItem = findShopItem(itemId);

        int totalPrice = shopItem.getPrice() * request.getCount();
        if (user.getGarlicCount() < totalPrice) {
            throw new BusinessException("마늘이 부족합니다.");
        }

        user.updateGarlicCount(user.getGarlicCount() - totalPrice);

        UserInventory inventory = memberInventoryRepository
                .findByUserAndItem(user, shopItem)
                .orElse(new UserInventory(user, shopItem, 0));
        inventory.addQuantity(request.getCount());
        memberInventoryRepository.save(inventory);

        PurchaseHistory history = purchaseHistoryRepository.save(
                new PurchaseHistory(user, shopItem, totalPrice)
        );

        return PurchaseResponse.builder()
                .purchaseId(history.getPurchaseId())
                .itemId(itemId)
                .remainingGarlic(user.getGarlicCount())
                .build();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private ShopItem findShopItem(Long itemId) {
        return shopItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 아이템입니다."));
    }

    private ShopItemResponse toShopItemResponse(ShopItem item) {
        return ShopItemResponse.builder()
                .itemId(item.getId())
                .itemName(item.getItemName())
                .itemType(item.getItemType())
                .itemImage(item.getItemImage())
                .price(item.getPrice())
                .build();
    }
}
