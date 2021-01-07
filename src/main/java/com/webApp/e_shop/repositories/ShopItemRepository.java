package com.webApp.e_shop.repositories;

import com.webApp.e_shop.entities.Brand;
import com.webApp.e_shop.entities.Category;
import com.webApp.e_shop.entities.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {

    List<ShopItem> findAllByAmountGreaterThanEqualOrderByPriceAsc(int amount);

    ShopItem findByAmountGreaterThanEqual(int amount);

    ShopItem findShopItemById(Long id);

    List<ShopItem> findByNameContainingOrderByPriceAsc(String name);
    List<ShopItem> findByNameContainingOrderByPriceDesc(String name);
//
//    List<ShopItem> findAllByNameContainingAndPriceBetweenOrderByPriceAsc(String name, double price1, double price2);
//    List<ShopItem> findAllByNameContainingAndPriceBetweenOrderByPriceDesc(String name, double price1, double price2);

    List<ShopItem> findAllByInTopPageTrue();

    List<ShopItem> findAllByBrand_IdAndNameContainingAndPriceBetweenOrderByPriceAsc(Long brand_id , String name, double price1, double price2);
    List<ShopItem> findAllByBrand_IdAndNameContainingAndPriceBetweenOrderByPriceDesc(Long brand_id , String name, double price1, double price2);

//    List<ShopItem> findAllByCategories(List<Category> cat);

}

