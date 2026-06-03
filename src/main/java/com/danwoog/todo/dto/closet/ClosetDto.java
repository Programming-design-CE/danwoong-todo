package com.danwoog.todo.dto.closet;

import lombok.*;

public class ClosetDto {

    @Getter @Builder
    public static class InventoryItemResponse {
        private Long itemId;
        private String itemName;
        private String itemType;
        private String itemImage;
        private Integer quantity;
    }

    @Getter @Builder
    public static class EquippedItemResponse {
        private Long itemId;
        private String itemName;
        private String slotType;
        private String itemImage;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor
    public static class EquipRequest {
        private Long itemId;
        private String slotType;  // HAT, CLOTHES, ACCESSORY, BACKGROUND, color
    }

    @Getter @Builder
    public static class EquipResponse {
        private Long characterId;
        private String characterThumbnailUrl;
    }

    @Getter @Builder
    public static class UseItemResponse {
        private Long itemId;
        private Integer quantity;
    }
}
