package com.starta.project.domain.mileageshop.repository;

import com.starta.project.domain.mileageshop.entity.ItemCategoryEnum;
import com.starta.project.domain.mileageshop.entity.MileageShopItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MileageShopItemRepository extends JpaRepository<MileageShopItem, Long> {
    List<MileageShopItem> findByCategory(ItemCategoryEnum category);
}