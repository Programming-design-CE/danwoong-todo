package com.danwoog.todo.repository;

import com.danwoog.todo.domain.CharacterEquippedItem;
import com.danwoog.todo.domain.UserCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterEquippedItemRepository extends JpaRepository<CharacterEquippedItem, Long> {

    List<CharacterEquippedItem> findByCharacter(UserCharacter character);
}