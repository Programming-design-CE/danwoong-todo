package com.danwoog.todo.global;

import com.danwoog.todo.domain.shop.ShopItem;
import com.danwoog.todo.repository.shop.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShopCatalogInitializer implements CommandLineRunner {

    private final ShopItemRepository shopItemRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<ShopSeedItem> seedItems = List.of(
                new ShopSeedItem("밀짚 모자", "HAT", "/assets/shop_hat_straw.svg", 120),
                new ShopSeedItem("동그란 안경", "ACCESSORY", "/assets/shop_accessory_glasses.svg", 140),
                new ShopSeedItem("노트북", "HAND_ACCESSORY", "/assets/shop_accessory_laptop.svg", 160)
        );

        List<ShopItem> existingCatalog = shopItemRepository.findAll();

        Map<String, ShopItem> existingItemsByName = existingCatalog.stream()
                .collect(Collectors.toMap(ShopItem::getItemName, Function.identity(), (left, right) -> left));
        Map<String, ShopItem> existingItemsByImage = existingCatalog.stream()
                .filter(item -> item.getItemImage() != null && !item.getItemImage().isBlank())
                .collect(Collectors.toMap(ShopItem::getItemImage, Function.identity(), (left, right) -> left));

        for (ShopSeedItem seedItem : seedItems) {
            ShopItem existing = existingItemsByName.get(seedItem.itemName());

            if (existing == null) {
                existing = existingItemsByImage.get(seedItem.itemImage());
            }

            if (existing != null) {
                existing.updateCatalog(seedItem.itemType(), seedItem.itemImage(), seedItem.price());
                continue;
            }

            shopItemRepository.save(
                    ShopItem.builder()
                            .itemName(seedItem.itemName())
                            .itemType(seedItem.itemType())
                            .itemImage(seedItem.itemImage())
                            .price(seedItem.price())
                            .build()
            );
        }
    }

    private record ShopSeedItem(String itemName, String itemType, String itemImage, Integer price) {
    }
}
