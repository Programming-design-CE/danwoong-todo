package com.danwoog.todo.dto.shop;

import lombok.*;

public class ShopDto {

    @Getter @Builder
    public static class ShopItemResponse {
        private Long itemId;
        private String itemName;
        private String itemType;
        private String itemImage;
        private Integer price;
    }

    @Getter @AllArgsConstructor
    public static class GarlicResponse {
        private Integer garlicCount;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class PurchaseRequest {
        private Integer count;
    }

    @Getter @Builder
    public static class PurchaseResponse {
        private Long purchaseId;
        private Long itemId;
        private Integer remainingGarlic;
    }
}
