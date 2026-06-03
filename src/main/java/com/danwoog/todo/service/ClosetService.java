package com.danwoog.todo.service;

import com.danwoog.todo.domain.shop.CharacterEquippedItem;
import com.danwoog.todo.domain.shop.ShopItem;
import com.danwoog.todo.domain.shop.UserCharacter;
import com.danwoog.todo.domain.shop.UserInventory;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.closet.ClosetDto.EquipRequest;
import com.danwoog.todo.dto.closet.ClosetDto.EquipResponse;
import com.danwoog.todo.dto.closet.ClosetDto.EquippedItemResponse;
import com.danwoog.todo.dto.closet.ClosetDto.InventoryItemResponse;
import com.danwoog.todo.dto.closet.ClosetDto.UseItemResponse;
import com.danwoog.todo.exception.CustomException.BusinessException;
import com.danwoog.todo.exception.CustomException.NotFoundException;
import com.danwoog.todo.repository.CharacterEquippedItemRepository;
import com.danwoog.todo.repository.shop.MemberInventoryRepository;
import com.danwoog.todo.repository.shop.ShopItemRepository;
import com.danwoog.todo.repository.user.UserCharacterRepository;
import com.danwoog.todo.repository.user.UserRepository;
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
                        .itemImage(inv.getItem().getItemImage())
                        .quantity(inv.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    public List<EquippedItemResponse> getEquippedItems(Long userId) {
        User user = findUser(userId);

        return userCharacterRepository.findByUser(user)
                .map(character -> equippedItemRepository.findByCharacter(character).stream()
                        .map(equipped -> EquippedItemResponse.builder()
                                .itemId(equipped.getItem().getId())
                                .itemName(equipped.getItem().getItemName())
                                .slotType(equipped.getSlotType())
                                .itemImage(equipped.getItem().getItemImage())
                                .build())
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    @Transactional
    public EquipResponse equipItem(Long userId, EquipRequest request) {
        User user = findUser(userId);
        UserCharacter character = findOrCreateCharacter(user);
        ShopItem item = findShopItem(request.getItemId());

        memberInventoryRepository.findByUserAndItem(user, item)
                .filter(inv -> inv.getQuantity() > 0)
                .orElseThrow(() -> new BusinessException("보유하지 않은 아이템입니다."));

        CharacterEquippedItem equipped = equippedItemRepository
                .findByCharacterAndSlotType(character, request.getSlotType())
                .map(existing -> {
                    existing.changeItem(item);
                    return existing;
                })
                .orElse(new CharacterEquippedItem(character, item, request.getSlotType()));
        equippedItemRepository.save(equipped);

        return EquipResponse.builder()
                .characterId(character.getCharacterId())
                .characterThumbnailUrl("/assets/danwoong.png")
                .build();
    }

    @Transactional
    public UseItemResponse useItem(Long userId, Long itemId) {
        User user = findUser(userId);
        ShopItem item = findShopItem(itemId);

        UserInventory inventory = memberInventoryRepository.findByUserAndItem(user, item)
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

    @Transactional
    private UserCharacter findOrCreateCharacter(User user) {
        return userCharacterRepository.findByUser(user)
                .orElseGet(() -> userCharacterRepository.save(new UserCharacter(user)));
    }

    private ShopItem findShopItem(Long itemId) {
        return shopItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 아이템입니다."));
    }
}
