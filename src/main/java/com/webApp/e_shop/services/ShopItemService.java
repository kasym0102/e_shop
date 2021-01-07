package com.webApp.e_shop.services;

import com.webApp.e_shop.entities.*;

import java.util.ArrayList;
import java.util.List;

public interface ShopItemService{

    ShopItem addItem(ShopItem item);

    List<ShopItem> getAllItems();

    ShopItem getItem(Long id);

    void deleteItemById(Long id);

    ShopItem saveItem(ShopItem shopItem);

//    List<ShopItem> findItemByName(String name);
//
    List<ShopItem> findItemByNameOrderByPriceDesc(String name);

    List<ShopItem> findItemByNameOrderByPriceAsc(String name);
//
//    List<ShopItem> findAllByNamePriceBetweenOrderByPriceDesc(String name, double price1, double price2);
//    List<ShopItem> findAllByNamePriceBetweenOrderByPriceAsc(String name, double price1, double price2);


    List<ShopItem> topPageItems();

    List<Brand> allBrands();

    Brand brandById(Long id);

    List<Country> allCountries();

    void addCountry(String name, String code);

    void addBrand(String name, Long country_id);

    List<ShopItem> allItems();

//    List<ShopItem> getItemsLikeBrandName(Long brand_id);

    List<ShopItem> itemsByBrand_idAndSearch_valueAndPricesAsc(Long brand_id, String name, double price_from, double price_to);
    List<ShopItem> itemsByBrand_idAndSearch_valueAndPricesDesc(Long brand_id, String name, double price_from, double price_to);

    Country getCountryById(Long id);

    void saveCountry(Country country);

    void saveBrand(Brand brand);

    List<Category> allCategories();

    void saveCategory(Category category);

    Category getCategoryById(Long id);

    void savePicture(Picture picture);

    List<Picture> picturesOfItem(Long itemId);

    Picture getPicture(Long id);

    void deletePicture(Long id);

    void savePurchase(Purchase purchase);

    List<Purchase> allPurchases();

    List<Comment> commentsOfItem(Long id);

    void saveComment(Comment comment);
    void deleteComment(Long id);

    Comment getComment(Long id);



}
