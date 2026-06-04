package com.danwoog.todo.controller;

import com.danwoog.todo.common.ApiResponse;
import com.danwoog.todo.dto.closet.ClosetDto.EquipRequest;
import com.danwoog.todo.dto.closet.ClosetDto.EquipResponse;
import com.danwoog.todo.dto.closet.ClosetDto.EquippedItemResponse;
import com.danwoog.todo.dto.closet.ClosetDto.InventoryItemResponse;
import com.danwoog.todo.dto.closet.ClosetDto.UnequipRequest;
import com.danwoog.todo.dto.closet.ClosetDto.UnequipResponse;
import com.danwoog.todo.dto.closet.ClosetDto.UseItemResponse;
import com.danwoog.todo.service.ClosetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/closet")
@RequiredArgsConstructor
public class ClosetController {

    private final ClosetService closetService;

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getInventoryItems(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.ok(closetService.getInventoryItems(getLoginUserId(authentication)))
        );
    }

    @GetMapping("/equipped-items")
    public ResponseEntity<ApiResponse<List<EquippedItemResponse>>> getEquippedItems(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.ok(closetService.getEquippedItems(getLoginUserId(authentication)))
        );
    }

    @PatchMapping("/equipped-items")
    public ResponseEntity<ApiResponse<EquipResponse>> equipItem(
            Authentication authentication,
            @RequestBody EquipRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(closetService.equipItem(getLoginUserId(authentication), request))
        );
    }

    @DeleteMapping("/equipped-items")
    public ResponseEntity<ApiResponse<UnequipResponse>> unequipItem(
            Authentication authentication,
            @RequestBody UnequipRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(closetService.unequipItem(getLoginUserId(authentication), request))
        );
    }

    @PostMapping("/items/{itemId}/use")
    public ResponseEntity<ApiResponse<UseItemResponse>> useItem(
            Authentication authentication,
            @PathVariable("itemId") Long itemId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(closetService.useItem(getLoginUserId(authentication), itemId))
        );
    }

    private Long getLoginUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}