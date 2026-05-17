package com.danwoog.todo.service;

import com.danwoog.todo.domain.*;
import com.danwoog.todo.dto.closet.ClosetDto.*;
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
public class ClosetService {

    private final MemberInventoryRepository memberInventoryRepository;
    private final UserCharacterRepository userCharacterRepository;
    private final CharacterEquippedItemRepository equippedItemRepository;
    private final ShopItemRepository shopItemRepository;
    private final UserRepository userRepository;

    public List<InventoryItemResponse> getInventoryItems(Long userId) {
        User user = findUser(userId);
        return memberInventoryRepository.findByUser(user).stream()
                .map(inv -> InventoryItemResponse.builder()
                        .itemId(inv.getItem().getId())
                        .itemName(inv.getItem().getItemName())
                        .itemType(inv.getItem().getItemType())
                        .quantity(inv.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public EquipResponse equipItem(Long userId, EquipRequest request) {
        User user = findUser(userId);
        UserCharacter character = findCharacter(user);
        ShopItem item = findShopItem(request.getItemId());

        memberInventoryRepository.findByUserAndItem(user, item)
                .filter(inv -> inv.getQuantity() > 0)
                .orElseThrow(() -> new BusinessException("보유하지 않은 아이템입니다."));

        // 슬롯 upsert: 있으면 교체, 없으면 신규
        CharacterEquippedItem equipped = equippedItemRepository
                .findByCharacterAndSlotType(character, request.getSlotType())
                .map(existing -> { existing.changeItem(item); return existing; })
                .orElse(new CharacterEquippedItem(character, item, request.getSlotType()));
        equippedItemRepository.save(equipped);

        return EquipResponse.builder()
                .characterId(character.getCharacterId())
                .characterThumbnailUrl("/characters/" + character.getCharacterId() + "/thumbnail")
                .build();
    }

    @Transactional
    public UseItemResponse useItem(Long userId, Long itemId) {
        User user = findUser(userId);
        ShopItem item = findShopItem(itemId);

        MemberInventory inventory = memberInventoryRepository.findByUserAndItem(user, item)
                .orElseThrow(() -> new NotFoundException("보유하지 않은 아이템입니다."));

        inventory.useQuantity(1);
        memberInventoryRepository.save(inventory);

        return UseItemResponse.builder()
                .itemId(itemId)
                .quantity(inventory.getQuantity())
                .build();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private UserCharacter findCharacter(User user) {
        return userCharacterRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("캐릭터가 존재하지 않습니다."));
    }

    private ShopItem findShopItem(Long itemId) {
        return shopItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 아이템입니다."));
    }
}
