package com.danwoog.todo.controller;

import com.danwoog.todo.common.ApiResponse;
import com.danwoog.todo.dto.closet.ClosetDto.*;
import com.danwoog.todo.service.ClosetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/closet")
@RequiredArgsConstructor
public class ClosetController {

    private final ClosetService closetService;

    private final Long TEMP_MEMBER_ID = 1L;

    /** GET /closet/items — 보유 아이템 목록 조회 */
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getInventoryItems() {
        return ResponseEntity.ok(ApiResponse.ok(closetService.getInventoryItems(TEMP_MEMBER_ID)));
    }

    /** PATCH /closet/equipped-items — 아이템 장착 */
    @PatchMapping("/equipped-items")
    public ResponseEntity<ApiResponse<EquipResponse>> equipItem(
            @RequestBody EquipRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(closetService.equipItem(TEMP_MEMBER_ID, request)));
    }

    /** POST /closet/items/{itemId}/use — 소비 아이템 사용 */
    @PostMapping("/items/{itemId}/use")
    public ResponseEntity<ApiResponse<UseItemResponse>> useItem(
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.ok(closetService.useItem(TEMP_MEMBER_ID, itemId)));
    }
}
