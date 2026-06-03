package com.danwoog.todo.controller;

import com.danwoog.todo.common.ApiResponse;
import com.danwoog.todo.dto.shop.ShopDto.GarlicResponse;
import com.danwoog.todo.dto.shop.ShopDto.PurchaseRequest;
import com.danwoog.todo.dto.shop.ShopDto.PurchaseResponse;
import com.danwoog.todo.dto.shop.ShopDto.ShopItemResponse;
import com.danwoog.todo.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/garlic")
    public ResponseEntity<ApiResponse<GarlicResponse>> getGarlic(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.ok(shopService.getGarlic(getLoginUserId(authentication)))
        );
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ShopItemResponse>>> getShopItems() {
        return ResponseEntity.ok(ApiResponse.ok(shopService.getShopItems()));
    }

    @PostMapping("/items/{itemId}/purchase")
    public ResponseEntity<ApiResponse<PurchaseResponse>> purchaseItem(
            Authentication authentication,
            @PathVariable("itemId") Long itemId,
            @RequestBody PurchaseRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        shopService.purchaseItem(getLoginUserId(authentication), itemId, request)
                )
        );
    }

    private Long getLoginUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
