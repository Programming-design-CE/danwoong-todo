package com.danwoog.todo.repository;

import com.danwoog.todo.domain.shop.CharacterEquippedItem;
import com.danwoog.todo.domain.shop.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterEquippedItemRepository extends JpaRepository<CharacterEquippedItem, Long> {
    List<CharacterEquippedItem> findByCharacter(UserCharacter character);
    Optional<CharacterEquippedItem> findByCharacterAndSlotType(UserCharacter character, String slotType);
}
