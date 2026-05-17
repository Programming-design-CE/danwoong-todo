package com.danwoog.todo.service;

import com.danwoog.todo.domain.*;
import com.danwoog.todo.dto.shop.ShopDto.*;
import com.danwoog.todo.exception.CustomException.*;
import com.danwoog.todo.repository.*;
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

        // 마늘 차감 — 팀원 A의 updateNickname() 스타일로 서비스에서 직접 계산
        int updatedGarlic = user.getGarlicCount() - totalPrice;
        user.updateGarlicCount(updatedGarlic);

        // 인벤토리 upsert
        MemberInventory inventory = memberInventoryRepository
                .findByUserAndItem(user, shopItem)
                .orElse(MemberInventory.builder().user(user).item(shopItem).quantity(0).build());
        inventory.addQuantity(request.getCount());
        memberInventoryRepository.save(inventory);

        // 구매 이력 저장
        PurchaseHistory history = purchaseHistoryRepository.save(
                PurchaseHistory.builder().user(user).item(shopItem).price(totalPrice).build()
        );

        return PurchaseResponse.builder()
                .purchaseId(history.getId())
                .itemId(itemId)
                .remainingGarlic(user.getGarlicCount())  // 차감 후 값
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
