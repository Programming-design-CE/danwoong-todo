package com.danwoog.todo.service;

import com.danwoog.todo.domain.shop.CharacterEquippedItem;
import com.danwoog.todo.domain.shop.ShopItem;
import com.danwoog.todo.domain.shop.UserCharacter;
import com.danwoog.todo.domain.shop.UserInventory;
import com.danwoog.todo.domain.user.User;
import com.danwoog.todo.dto.closet.ClosetDto.*;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<InventoryItemResponse> result = new ArrayList<>();
        for (UserInventory inv : memberInventoryRepository.findByUser(user)) {
            result.add(InventoryItemResponse.builder()
                    .itemId(inv.getItem().getId())
                    .itemName(inv.getItem().getItemName())
                    .itemType(inv.getItem().getItemType())
                    .itemImage(inv.getItem().getItemImage())
                    .quantity(inv.getQuantity())
                    .build());
        }
        return result;
    }

    public List<EquippedItemResponse> getEquippedItems(Long userId) {
        User user = findUser(userId);
        if (!userCharacterRepository.findByUser(user).isPresent()) {
            return Collections.emptyList();
        }
        UserCharacter character = userCharacterRepository.findByUser(user).get();
        List<EquippedItemResponse> result = new ArrayList<>();
        for (CharacterEquippedItem equipped : equippedItemRepository.findByCharacter(character)) {
            result.add(EquippedItemResponse.builder()
                    .itemId(equipped.getItem().getId())
                    .itemName(equipped.getItem().getItemName())
                    .slotType(equipped.getSlotType())
                    .itemImage(equipped.getItem().getItemImage())
                    .build());
        }
        return result;
    }

    @Transactional
    public EquipResponse equipItem(Long userId, EquipRequest request) {
        User user = findUser(userId);
        UserCharacter character = findOrCreateCharacter(user);
        ShopItem item = findShopItem(request.getItemId());
        String slotType = request.getSlotType();

        memberInventoryRepository.findByUserAndItem(user, item)
                .filter(inv -> inv.getQuantity() > 0)
                .orElseThrow(() -> new BusinessException("보유하지 않은 아이템입니다."));

        CharacterEquippedItem existing =
                equippedItemRepository.findByCharacterAndSlotType(character, slotType).orElse(null);

        CharacterEquippedItem equipped;
        if (existing != null) {
            existing.changeItem(item);
            equipped = existing;
        } else {
            equipped = new CharacterEquippedItem(character, item, slotType);
        }
        equippedItemRepository.save(equipped);

        return EquipResponse.builder()
                .characterId(character.getCharacterId())
                .characterThumbnailUrl("/assets/danwoong.png")
                .build();
    }

    @Transactional
    public UnequipResponse unequipItem(Long userId, UnequipRequest request) {
        User user = findUser(userId);
        Long itemId = request.getItemId();
        ShopItem item = findShopItem(itemId);

        UserCharacter character = userCharacterRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("캐릭터를 찾을 수 없습니다."));

        CharacterEquippedItem target = null;
        for (CharacterEquippedItem e : equippedItemRepository.findByCharacter(character)) {
            if (e.getItem().getId().equals(item.getId())) {
                target = e;
                break;
            }
        }
        if (target == null) {
            throw new BusinessException("착용 중이지 않은 아이템입니다.");
        }

        equippedItemRepository.delete(target);

        return UnequipResponse.builder()
                .itemId(itemId)
                .message("아이템 착용이 해제되었습니다.")
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

    private UserCharacter findOrCreateCharacter(User user) {
        if (userCharacterRepository.findByUser(user).isPresent()) {
            return userCharacterRepository.findByUser(user).get();
        }
        return userCharacterRepository.save(new UserCharacter(user));
    }

    private ShopItem findShopItem(Long itemId) {
        return shopItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 아이템입니다."));
    }
}