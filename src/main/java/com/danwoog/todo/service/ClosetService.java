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
import com.danwoog.todo.dto.closet.ClosetDto.UnequipRequest;
import com.danwoog.todo.dto.closet.ClosetDto.UnequipResponse;
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
import java.util.List;
import java.util.Optional;

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
        List<UserInventory> inventories = memberInventoryRepository.findByUser(user);
        List<InventoryItemResponse> result = new ArrayList<>();
        for (UserInventory inv : inventories) {
            InventoryItemResponse response = InventoryItemResponse.builder()
                    .itemId(inv.getItem().getId())
                    .itemName(inv.getItem().getItemName())
                    .itemType(inv.getItem().getItemType())
                    .itemImage(inv.getItem().getItemImage())
                    .quantity(inv.getQuantity())
                    .build();
            result.add(response);
        }
        return result;
    }

    public List<EquippedItemResponse> getEquippedItems(Long userId) {
        User user = findUser(userId);
        Optional<UserCharacter> characterOpt = userCharacterRepository.findByUser(user);
        if (!characterOpt.isPresent()) {
            return new ArrayList<>();
        }
        UserCharacter character = characterOpt.get();
        List<CharacterEquippedItem> equippedItems = equippedItemRepository.findByCharacter(character);
        List<EquippedItemResponse> result = new ArrayList<>();
        for (CharacterEquippedItem equipped : equippedItems) {
            EquippedItemResponse response = EquippedItemResponse.builder()
                    .itemId(equipped.getItem().getId())
                    .itemName(equipped.getItem().getItemName())
                    .slotType(equipped.getSlotType())
                    .itemImage(equipped.getItem().getItemImage())
                    .build();
            result.add(response);
        }
        return result;
    }

    @Transactional
    public EquipResponse equipItem(Long userId, EquipRequest request) {
        User user = findUser(userId);
        UserCharacter character = findOrCreateCharacter(user);
        ShopItem item = findShopItem(request.getItemId());
        String slotType = request.getSlotType();

        // 1. 인벤토리 보유 여부 검증
        memberInventoryRepository.findByUserAndItem(user, item)
                .filter(inv -> inv.getQuantity() > 0)
                .orElseThrow(() -> new BusinessException("보유하지 않은 아이템입니다."));

        // 2. 캐릭터가 장착 중인 전체 아이템 조회
        List<CharacterEquippedItem> currentEquipped = equippedItemRepository.findByCharacter(character);

        // [방어 로직] 이미 똑같은 아이템을 착용 중인지 확인
        boolean isAlreadyEquipped = currentEquipped.stream()
                .anyMatch(e -> e.getItem().getId().equals(item.getId()));
        if (isAlreadyEquipped) {
            throw new BusinessException("이미 착용 중인 아이템입니다.");
        }

        // 3. 다중 장착 로직 삭제! 무조건 같은 슬롯이면 갈아끼운다 (교체)
        CharacterEquippedItem equipped = currentEquipped.stream()
                .filter(e -> e.getSlotType().equals(slotType)) // 부위가 같으면
                .findFirst()
                .map(existing -> {
                    existing.changeItem(item); // 기존 아이템을 새 아이템으로 교체!
                    return existing;
                })
                .orElseGet(() -> new CharacterEquippedItem(character, item, slotType));
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

        // 캐릭터가 장착 중인 아이템 중 요청된 itemId와 일치하는 것을 찾아서 삭제
        CharacterEquippedItem target = equippedItemRepository.findByCharacter(character).stream()
                .filter(e -> e.getItem().getId().equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("착용 중이지 않은 아이템입니다."));

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

    @Transactional
    private UserCharacter findOrCreateCharacter(User user) {
        Optional<UserCharacter> existing = userCharacterRepository.findByUser(user);
        if (existing.isPresent()) {
            return existing.get();
        }
        return userCharacterRepository.save(new UserCharacter(user));
    }

    private ShopItem findShopItem(Long itemId) {
        return shopItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 아이템입니다."));
    }
}