package com.danwoog.todo.repository;

<<<<<<< HEAD
import com.danwoog.todo.domain.CharacterEquippedItem;
import com.danwoog.todo.domain.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterEquippedItemRepository extends JpaRepository<CharacterEquippedItem, Long> {
    List<CharacterEquippedItem> findByCharacter(UserCharacter character);
    // slotType 기준 upsert용 (내 ClosetService에서 사용)
    Optional<CharacterEquippedItem> findByCharacterAndSlotType(UserCharacter character, String slotType);
}
=======
import com.danwoog.todo.domain.shop.CharacterEquippedItem;
import com.danwoog.todo.domain.shop.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterEquippedItemRepository extends JpaRepository<CharacterEquippedItem, Long> {

    List<CharacterEquippedItem> findByCharacter(UserCharacter character);
}
>>>>>>> main
