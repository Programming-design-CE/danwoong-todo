package com.danwoog.todo.controller;

import com.danwoog.todo.common.ApiResponse;
import com.danwoog.todo.dto.shop.ShopDto.*;
import com.danwoog.todo.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    // 팀원 코드와 동일하게 임시 하드코딩 (인증 붙으면 교체)
    private final Long TEMP_MEMBER_ID = 1L;

    /** GET /shop/garlic — 보유 마늘 조회 */
    @GetMapping("/garlic")
    public ResponseEntity<ApiResponse<GarlicResponse>> getGarlic() {
        return ResponseEntity.ok(ApiResponse.ok(shopService.getGarlic(TEMP_MEMBER_ID)));
    }

    /** GET /shop/items — 판매 아이템 목록 조회 */
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> getShopItems() {
        return ResponseEntity.ok(ApiResponse.ok(shopService.getShopItems()));
    }

    /** POST /shop/items/{itemId}/purchase — 아이템 구매 */
    @PostMapping("/items/{itemId}/purchase")
    public ResponseEntity<ApiResponse<PurchaseResponse>> purchaseItem(
            @PathVariable Long itemId,
            @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(shopService.purchaseItem(TEMP_MEMBER_ID, itemId, request)));
    }
}
