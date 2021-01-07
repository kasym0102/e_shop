package com.webApp.e_shop.Impl;

import com.webApp.e_shop.entities.*;
import com.webApp.e_shop.repositories.*;
import com.webApp.e_shop.services.ShopItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ShopItemServiceImpl implements ShopItemService {

    @Autowired
    private ShopItemRepository itemRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public ShopItem addItem(ShopItem item) {
        return itemRepository.save(item);
    }

    @Override
    public List<ShopItem> getAllItems() {
        return itemRepository.findAllByAmountGreaterThanEqualOrderByPriceAsc(0);
    }

    @Override
    public ShopItem getItem(Long id) {
        return itemRepository.findShopItemById(id);
    }

    @Override
    public void deleteItemById(Long id) {
        itemRepository.deleteById(id);
    }


    @Override
    public ShopItem saveItem(ShopItem shopItem) {
        return itemRepository.save(shopItem);
    }


    @Override
    public List<ShopItem> findItemByNameOrderByPriceAsc(String name) {
        return itemRepository.findByNameContainingOrderByPriceAsc(name);
    }

    @Override
    public List<ShopItem> findItemByNameOrderByPriceDesc(String name) {
        return itemRepository.findByNameContainingOrderByPriceDesc(name);
    }

    @Override
    public List<ShopItem> topPageItems() {
        return itemRepository.findAllByInTopPageTrue();
    }

    @Override
    public List<Brand> allBrands() {
        return brandRepository.findAll();
    }

    @Override
    public Brand brandById(Long id) {
        return brandRepository.getOne(id);
    }

    @Override
    public List<Country> allCountries() {
        return countryRepository.findAll();
    }

    @Override
    public void addCountry(String name, String code) {
        countryRepository.save(new Country(null, name, code));
    }

    @Override
    public void addBrand(String name, Long country_id) {
        brandRepository.save(new Brand(null, name, countryRepository.getOne(country_id)));
    }

    @Override
    public List<ShopItem> allItems() {
        return itemRepository.findAll();
    }

    @Override
    public List<ShopItem> itemsByBrand_idAndSearch_valueAndPricesAsc(Long brand_id, String name, double price_from, double price_to) {
        return itemRepository.findAllByBrand_IdAndNameContainingAndPriceBetweenOrderByPriceAsc(brand_id, name, price_from, price_to);
    }

    @Override
    public List<ShopItem> itemsByBrand_idAndSearch_valueAndPricesDesc(Long brand_id, String name, double price_from, double price_to) {
        return itemRepository.findAllByBrand_IdAndNameContainingAndPriceBetweenOrderByPriceDesc(brand_id, name, price_from, price_to);
    }

    @Override
    public Country getCountryById(Long id) {
        return countryRepository.getOne(id);
    }

    @Override
    public void saveCountry(Country country) {
        countryRepository.save(country);
    }

    @Override
    public void saveBrand(Brand brand) {
        brandRepository.save(brand);
    }

    @Override
    public List<Category> allCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.getOne(id);
    }

    @Override
    public void savePicture(Picture picture) {
        pictureRepository.save(picture);
    }

    @Override
    public List<Picture> picturesOfItem(Long itemId) {
        return pictureRepository.findAllByShopItemId(itemId);
    }

    @Override
    public Picture getPicture(Long id) {
        return pictureRepository.getOne(id);
    }

    @Override
    public void deletePicture(Long id) {
        pictureRepository.deleteById(id);
    }

    @Override
    public void savePurchase(Purchase purchase) {
        purchaseRepository.save(purchase);
    }

    @Override
    public List<Purchase> allPurchases() {
        return purchaseRepository.findAll();
    }

    @Override
    public List<Comment> commentsOfItem(Long id) {
        return commentRepository.findAllByShopItemId(id);
    }

    @Override
    public void saveComment(Comment comment) {
        commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public Comment getComment(Long id) {
        return commentRepository.findAllById(id);
    }
}
