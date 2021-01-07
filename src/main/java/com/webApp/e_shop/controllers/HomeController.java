package com.webApp.e_shop.controllers;

import com.webApp.e_shop.Impl.ShopItemServiceImpl;
import com.webApp.e_shop.entities.*;
import com.webApp.e_shop.services.ShopUserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.jni.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class HomeController {


    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ShopItemServiceImpl shopItemService;

    @Autowired
    private ShopUserService shopUserService;

    @Value("${file.avatar.viewPath}")
    private String viewPath;

    @Value("${file.avatar.uploadPath}")
    private String uploadPath;

    @Value("${file.avatar.defaultPicture}")
    private String defaultPicture;


    @GetMapping(value = "/")
    public String index(Model model, HttpServletRequest request) {
        String n = "EN";
//        for (Cookie cookie: request.getCookies()) {
//            if (cookie.getName().equals("lng"));
//            n = cookie.getValue();
//        }
        model.addAttribute("lan_name", "eng");
        List<ShopItem> items = shopItemService.getAllItems();

        List<ShopItem> topPageItems = shopItemService.topPageItems();

        items.removeAll(topPageItems);

        topPageItems.addAll(items);
        model.addAttribute("items", topPageItems);

        List<Brand> brands = shopItemService.allBrands();
        model.addAttribute("brands", brands);

        model.addAttribute("currentUser", getUserData());


        return "index";
    }


    @GetMapping(value = "/search")
    public String search(@RequestParam(name = "search_value", defaultValue = "") String search_value,
                         @RequestParam(name = "isAsc", defaultValue = "true") boolean isAsc,
                         Model model) {
        List<ShopItem> items;
        if (isAsc) {
            items = shopItemService.findItemByNameOrderByPriceAsc(search_value);
        } else {
            items = shopItemService.findItemByNameOrderByPriceDesc(search_value);
        }

        List<Brand> brands = shopItemService.allBrands();

        model.addAttribute("brands", brands);
        model.addAttribute("search_value", search_value);
        model.addAttribute("isAsc", isAsc);
        model.addAttribute("items", items);

        model.addAttribute("currentUser", getUserData());
        return "search";

    }

    @GetMapping(value = "/FullSearch")
    public String FullSearch(@RequestParam(name = "search_value", defaultValue = "") String search_value,
                             @RequestParam(name = "price_from") Double price_from,
                             @RequestParam(name = "price_to") Double price_to,
                             @RequestParam(name = "isAsc", defaultValue = "true") boolean isAsc,
                             @RequestParam(name = "brand_id", defaultValue = "") Long brand_id,
                             Model model) {
        double price_from1 = Double.MIN_VALUE;
        double price_to1 = Double.MAX_VALUE;
        if (price_from != null) {
            price_from1 = price_from;
        }
        if (price_to != null) {
            price_to1 = price_to;
        }
        List<ShopItem> items;
        if (isAsc) {
            items = shopItemService.itemsByBrand_idAndSearch_valueAndPricesAsc(brand_id, search_value, price_from1, price_to1);
        } else {
            items = shopItemService.itemsByBrand_idAndSearch_valueAndPricesDesc(brand_id, search_value, price_from1, price_to1);
        }

        List<Brand> brands = shopItemService.allBrands();

        model.addAttribute("brands", brands);
        model.addAttribute("selected_brand_id", brand_id);
        model.addAttribute("search_value", search_value);
        model.addAttribute("price_from", price_from);
        model.addAttribute("price_to", price_to);
        model.addAttribute("isAsc", isAsc);
        model.addAttribute("items", items);

        model.addAttribute("currentUser", getUserData());


        return "search";
    }

    @GetMapping(value = "/itemDetails/{id}")
    public String ItemDetails(Model model, @PathVariable(name = "id") Long id) {

        ShopItem item = shopItemService.getItem(id);
        List<Brand> brands = shopItemService.allBrands();
        List<Picture> pictures = shopItemService.picturesOfItem(id);

        List<Comment> comments = shopItemService.commentsOfItem(id);

        model.addAttribute("item", item);
        model.addAttribute("brands", brands);
        model.addAttribute("pictures", pictures);
        model.addAttribute("comments", comments);
        model.addAttribute("edit_comment_id", editing_comment_id);

        model.addAttribute("currentUser", getUserData());

        return "itemDetails";
    }


    @PostMapping(value = "/uploadAvatar")
    @PreAuthorize("isAuthenticated()")
    public String uploadAvatar(@RequestParam(name = "user_avatar") MultipartFile user_avatar) {

        if (user_avatar.getContentType().equals("image/jpeg") || user_avatar.getContentType().equals("image/png")) {

            try {
                ShopUser currentUser = getUserData();

                String picName = DigestUtils.sha1Hex("avatar_" + currentUser.getId() + "_picture!");

                byte[] bytes = user_avatar.getBytes();
                Path path = Paths.get(uploadPath + picName + ".jpg");

                Files.write(path, bytes);

                currentUser.setPictureURL(picName);

                shopUserService.saveUser(currentUser);



                return "redirect:/profile?success";

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "redirect:/profile?error";
    }


    @GetMapping(value = "/viewPhoto/{url}", produces = {MediaType.IMAGE_JPEG_VALUE})
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody
    byte[] viewPhoto(@PathVariable(name = "url") String photo_url) throws IOException {

        String pictureUrl = viewPath + defaultPicture;

        if (photo_url != null && photo_url.equals(getUserData().getPictureURL())) {
            pictureUrl = viewPath + photo_url + ".jpg";
        }

        InputStream in;

        try {
            ClassPathResource resource = new ClassPathResource(pictureUrl);
            in = resource.getInputStream();
        } catch (Exception e) {

            ClassPathResource resource = new ClassPathResource(viewPath + defaultPicture);
            in = resource.getInputStream();
            e.printStackTrace();
        }

        return IOUtils.toByteArray(in);

    }

    //admin************************************************************************************************
//************************************************************************************************
//************************************************************************************************
    @GetMapping(value = "/admin")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String admin() {

        String ent_name = "items";

        return "redirect:/admin/" + ent_name;
    }

    @GetMapping(value = "/admin/{entity_name}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String admin_ent(Model model,
                            @PathVariable(name = "entity_name") String ent_name) {

        if (getUserData().getRoles().size() == 3) {

            List<Country> countries = shopItemService.allCountries();
            List<Brand> brands = shopItemService.allBrands();
            List<Category> categories = shopItemService.allCategories();
            List<ShopUser> users = shopUserService.allUsers();
            List<ShopUser> moderators = shopUserService.allUsers();
            List<Purchase> purchases = shopItemService.allPurchases();

            List<Role> roles = shopUserService.allRoles();

            if (users != null) {
                for (ShopUser shopUser : shopUserService.allUsers()) {
                    if (shopUser.getRoles().size() > 1) {
                        users.remove(shopUser);
                    }
                }
            }

            if (moderators != null) {
                for (ShopUser shopUser : shopUserService.allUsers()) {
                    if (shopUser.getRoles().size() != 2) {
                        moderators.remove(shopUser);
                    }
                }
            }
            model.addAttribute("brands", brands);
            model.addAttribute("countries", countries);
            model.addAttribute("categories", categories);
            model.addAttribute("users", users);
            model.addAttribute("moderators", moderators);
            model.addAttribute("roles", roles);
            model.addAttribute("purchases", purchases);
        } else {
            ent_name = "items";
        }

        List<ShopItem> items = shopItemService.allItems();

        model.addAttribute("items", items);

        model.addAttribute("entity_name", ent_name);


        model.addAttribute("currentUser", getUserData());

        model.addAttribute("rolesCount", getUserData().getRoles().size());


        return "admin/admin";
    }

    @PostMapping(value = "/addItem")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String addTask(@RequestParam(name = "name", defaultValue = "Any Item") String name,
                          @RequestParam(name = "description", defaultValue = "Description of this item") String description,
                          @RequestParam(name = "price", defaultValue = "0") double price,
                          @RequestParam(name = "amount", defaultValue = "0") int amount,
                          @RequestParam(name = "stars", defaultValue = "0") int stars,
                          @RequestParam(name = "small_picture_url", defaultValue = "http://www.argos-informatique.com/sites/default/files/images_site/pagematerielneuf/mondeapple.jpg") String small_picture_url,
                          @RequestParam(name = "large_picture_url", defaultValue = "http://www.argos-informatique.com/sites/default/files/images_site/pagematerielneuf/mondeapple.jpg") String large_picture_url,
                          @RequestParam(name = "inTopPage", defaultValue = "Any Item") boolean inTopPage,
                          @RequestParam(name = "brand_id", defaultValue = "0") Long brand_id
    ) {
        Brand br = shopItemService.brandById(brand_id);
        shopItemService.addItem(new ShopItem(null, name, description, price, amount, stars, small_picture_url, large_picture_url, new Date(), inTopPage, br));
        return "redirect:/admin/items";

    }


    @PostMapping(value = "/deleteItem")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")

    public String deleteItem(@RequestParam(name = "delete_id", defaultValue = "0") Long id) {
        shopItemService.deleteItemById(id);
        return "redirect:/admin/items";

    }

    @GetMapping(value = "/editItem")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String EditItem(Model model, @RequestParam(name = "id") Long id) {

        ShopItem item = shopItemService.getItem(id);
        List<Brand> brands = shopItemService.allBrands();
        List<Category> categories = shopItemService.allCategories();
        List<Picture> pictures = shopItemService.picturesOfItem(id);
        categories.removeAll(item.getCategories());

        List<Country> countries = shopItemService.allCountries();
        List<ShopUser> users = shopUserService.allUsers();
        List<ShopUser> moderators = shopUserService.allUsers();
        List<Purchase> purchases = shopItemService.allPurchases();
        List<Role> roles = shopUserService.allRoles();

        model.addAttribute("brands", brands);
        model.addAttribute("categories", categories);
        model.addAttribute("item", item);
        model.addAttribute("countries", countries);
        model.addAttribute("users", users);
        model.addAttribute("moderators", moderators);
        model.addAttribute("roles", roles);
        model.addAttribute("purchases", purchases);
        model.addAttribute("pictures", pictures);
        model.addAttribute("currentUser", getUserData());

        return "admin/edit_delete/editItem";
    }


    @PostMapping(value = "/saveItem")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String saveItem(@RequestParam(name = "id", defaultValue = "0") Long id,
                           @RequestParam(name = "name", defaultValue = "Any Item") String name,
                           @RequestParam(name = "description", defaultValue = "Description of this item") String description,
                           @RequestParam(name = "price", defaultValue = "0") double price,
                           @RequestParam(name = "amount", defaultValue = "0") int amount,
                           @RequestParam(name = "stars", defaultValue = "0") int stars,
                           @RequestParam(name = "small_picture_url", defaultValue = "http://www.argos-informatique.com/sites/default/files/images_site/pagematerielneuf/mondeapple.jpg") String small_picture_url,
                           @RequestParam(name = "large_picture_url", defaultValue = "http://www.argos-informatique.com/sites/default/files/images_site/pagematerielneuf/mondeapple.jpg") String large_picture_url,
                           @RequestParam(name = "inTopPage", defaultValue = "Any Item") boolean inTopPage,
                           @RequestParam(name = "brand_id", defaultValue = "0") Long brand_id
    ) {

        ShopItem item = shopItemService.getItem(id);
        Brand brand = shopItemService.brandById(brand_id);
        if (item != null) {
            item.setName(name);
            item.setDescription(description);
            item.setPrice(price);
            item.setStars(stars);
            item.setAmount(amount);
            item.setSmallPicURL(small_picture_url);
            item.setLargePicURL(large_picture_url);
            item.setInTopPage(inTopPage);
            item.setBrand(brand);
            shopItemService.saveItem(item);
        }

        return "redirect:/admin/items";

    }


    @PostMapping(value = "/addCountry")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addCountry(@RequestParam(name = "name", defaultValue = "") String name,
                             @RequestParam(name = "code", defaultValue = "") String code
    ) {
        shopItemService.addCountry(name, code);
        return "redirect:/admin/countries";

    }

    @PostMapping(value = "/addBrand")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addBrand(@RequestParam(name = "name", defaultValue = "") String name,
                           @RequestParam(name = "country_id", defaultValue = "") Long country_id
    ) {
        shopItemService.addBrand(name, country_id);
        return "redirect:/admin/brands";

    }


    @GetMapping(value = "/editCountry")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String editCountry(Model model, @RequestParam(name = "id") Long id) {

        Country country = shopItemService.getCountryById(id);
        model.addAttribute("country", country);

        model.addAttribute("currentUser", getUserData());

        return "admin/edit_delete/editCountry";
    }


    @PostMapping(value = "/editCountry")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String saveItem(@RequestParam(name = "edit_id", defaultValue = "0") Long id,
                           @RequestParam(name = "edit_name", defaultValue = "Any Item") String name,
                           @RequestParam(name = "edit_code", defaultValue = "Code...") String code
    ) {

        Country country = shopItemService.getCountryById(id);
        if (country != null) {
            country.setName(name);
            country.setCode(code);
            shopItemService.saveCountry(country);
        }


        return "redirect:/admin/countries";
    }


    @GetMapping(value = "/editBrand")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String editBrand(Model model, @RequestParam(name = "id") Long id) {

        Brand brand = shopItemService.brandById(id);
        List<Country> countries = shopItemService.allCountries();
        model.addAttribute("brand", brand);
        model.addAttribute("countries", countries);

        model.addAttribute("currentUser", getUserData());

        return "admin/edit_delete/editBrand";
    }


    @PostMapping(value = "/editBrand")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String saveBrand(@RequestParam(name = "edit_id", defaultValue = "0") Long id,
                            @RequestParam(name = "edit_name", defaultValue = "Any name") String name,
                            @RequestParam(name = "country_id", defaultValue = "Code...") Long country_id
    ) {

        Country country = shopItemService.getCountryById(country_id);
        Brand brand = shopItemService.brandById(id);
        if (country != null && brand != null) {
            brand.setName(name);
            brand.setCountry(country);
            shopItemService.saveBrand(brand);
        }

        return "redirect:/admin/brands";

    }


    @PostMapping(value = "/addCategory")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addCategories(@RequestParam(name = "name", defaultValue = "") String name,
                                @RequestParam(name = "logoURL", defaultValue = "") String logoURL
    ) {
        shopItemService.saveCategory(new Category(null, name, logoURL));
        return "redirect:/admin/categories";

    }


    @PostMapping(value = "/addCategoryToItem")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addCategoryToItem(@RequestParam(name = "item_id", defaultValue = "0") Long item_id,
                                    @RequestParam(name = "category_id", defaultValue = "Any Item") Long category_id

    ) {

        ShopItem item = shopItemService.getItem(item_id);
        if (item != null) {

            item.addCategory(shopItemService.getCategoryById(category_id));
            shopItemService.saveItem(item);
        }

        return "redirect:/editItem?id=" + item_id;

    }

    @PostMapping(value = "/deleteCategoryFromItem")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String deleteCategoryFromItem(@RequestParam(name = "item_id", defaultValue = "0") Long item_id,
                                         @RequestParam(name = "category_id", defaultValue = "0") Long category_id

    ) {

        ShopItem item = shopItemService.getItem(item_id);
        if (item != null) {

            item.deleteCategory(shopItemService.getCategoryById(category_id));
            shopItemService.saveItem(item);
        }

        return "redirect:/editItem?id=" + item_id;

    }


    @GetMapping(value = "/editCategory")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String editCategory(Model model, @RequestParam(name = "id") Long id) {

        Category category = shopItemService.getCategoryById(id);
        model.addAttribute("category", category);

        model.addAttribute("currentUser", getUserData());

        return "admin/edit_delete/editCategory";
    }

    @PostMapping(value = "/editCategory")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String saveBrand(@RequestParam(name = "name", defaultValue = "") String name,
                            @RequestParam(name = "logoURL", defaultValue = "") String logoURL,
                            @RequestParam(name = "category_id", defaultValue = "") Long category_id
    ) {

        Category category = shopItemService.getCategoryById(category_id);
        if (category != null) {
            category.setName(name);
            category.setLogoURL(logoURL);
            shopItemService.saveCategory(category);
        }

        return "redirect:/admin/categories";

    }


    @GetMapping(value = "/403")
    public String accessDenied(Model model) {
        model.addAttribute("currentUser", getUserData());
        List<Brand> brands = shopItemService.allBrands();

        model.addAttribute("brands", brands);

        return "403";
    }

    @GetMapping(value = "/login")
    public String login(Model model) {

        return "login";
    }

    @GetMapping(value = "/profile")
    @PreAuthorize("isAuthenticated()")
    public String profile(Model model) {

//        model.addAttribute("mes", mes);
        model.addAttribute("currentUser", getUserData());
        List<Brand> brands = shopItemService.allBrands();

        model.addAttribute("brands", brands);
        return "profile";
    }

    private ShopUser getUserData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            User secUser = (User) authentication.getPrincipal();
            ShopUser shopUser = shopUserService.getShopUserByEmail(secUser.getUsername());
            return shopUser;
        }
        return null;
    }


    @GetMapping(value = "/registration")
    public String registration(Model model) {

        return "registration";
    }

    @PostMapping(value = "/registr")
//    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String toRegistration(@RequestParam(name = "fullName") String fullName,
                                 @RequestParam(name = "user_email") String email,
                                 @RequestParam(name = "user_password") String password,
                                 @RequestParam(name = "re_user_password") String re_password
    ) {

        if (password.equals(re_password)) {
            if (shopUserService.createUser(new ShopUser(email, password, fullName)) != null) {
                if (getUserData() != null) {
                    if (getUserData().getRoles().size() == 3) {
                        return "redirect:/admin/users";
                    }
                }
                return "redirect:/login";
            }

        }
        if (getUserData() != null) {
            if (getUserData().getRoles().size() == 3) {
                return "redirect:/admin/users?error";
            }
        }
        return "redirect:/registration?error";


    }


    @PostMapping(value = "/registrModerator")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String toRegistrationModerator(@RequestParam(name = "fullName") String fullName,
                                          @RequestParam(name = "user_email") String email,
                                          @RequestParam(name = "user_password") String password,
                                          @RequestParam(name = "re_user_password") String re_password
    ) {

        if (password.equals(re_password)) {
            if (shopUserService.createModerator(new ShopUser(email, password, fullName)) != null)
                return "redirect:/admin/moderators";
        }

        return "redirect:/admin/moderators?error";


    }


    @GetMapping(value = "/editUser")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String EditUser(Model model, @RequestParam(name = "id") Long id) {

        ShopUser user = shopUserService.getShopUserById(id);

//        ShopItem item = shopItemService.getItem(id);
//        List<Brand> brands = shopItemService.allBrands();
//        List<Category> categories = shopItemService.allCategories();
//        categories.removeAll(item.getCategories());

        model.addAttribute("user", user);
//        model.addAttribute("categories", categories);
//        model.addAttribute("item", item);

        model.addAttribute("currentUser", getUserData());

        return "admin/edit_delete/editUser";
    }


    @PostMapping(value = "/saveUser")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String saveUser(@RequestParam(name = "id") Long id,
                           @RequestParam(name = "fullName") String fullName,
                           @RequestParam(name = "user_email") String email,
                           @RequestParam(name = "user_password") String password,
                           @RequestParam(name = "re_user_password") String re_user_password

    ) {

        ShopUser user = shopUserService.getShopUserById(id);

        if (user != null) {
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));

            if (password.equals(re_user_password)) {
                if (shopUserService.saveUser(user)) {
                    return "redirect:/admin/users";
                }
            }
        }
        return "redirect:/admin/moderators?error";

    }


    @PostMapping(value = "/updateProfile")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(@RequestParam(name = "user_id") Long id,
                                @RequestParam(name = "user_fullName") String fullName

    ) {

        ShopUser user = shopUserService.getShopUserById(id);

        if (user != null) {
            user.setFullName(fullName);
            if (shopUserService.updateProfile(user)) {
                return "redirect:/profile";
            }

        }
        return "redirect:/profile?error";

    }

    @PostMapping(value = "/updatePassword")
    @PreAuthorize("isAuthenticated()")
    public String updatePassword(@RequestParam(name = "user_id") Long id,
                                 @RequestParam(name = "old_password") String old_password,
                                 @RequestParam(name = "new_password") String new_pass,
                                 @RequestParam(name = "re_new_password") String re_new_pass

    ) {

        ShopUser user = shopUserService.getShopUserById(id);

        if (user != null) {
            if (new_pass.equals(re_new_pass)) {
                if (passwordEncoder.matches(old_password, getUserData().getPassword())) {
                    user.setPassword(passwordEncoder.encode(new_pass));
                    if (shopUserService.updatePassword(user)) {
                        return "redirect:/profile?success";
                    }
                }

            }

        }
        return "redirect:/profile?error";

    }


    @PostMapping(value = "/addRole")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addRole(@RequestParam(name = "name") String name,
                          @RequestParam(name = "description") String description

    ) {

        Role role = shopUserService.getRoleByName(name);

        if (role != null) {

            return "redirect:/admin/roles?error";

        }

        shopUserService.saveRole(new Role(null, name, description));
        return "redirect:/admin/roles?success";

    }


    @PostMapping(value = "/uploadPictureToItem")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String uploadPictureToItem(@RequestParam(name = "item_picture") MultipartFile item_picture,
                                      @RequestParam(name = "item_id") Long item_id
    ) {

        if (item_picture.getContentType().equals("image/jpeg") || item_picture.getContentType().equals("image/png")) {

            try {

                List<Picture> pictures = shopItemService.picturesOfItem(item_id);

                Long picId = 1L;

                if (pictures != null && pictures.size() > 0) {
                    picId = pictures.get(pictures.size() - 1).getId();
                }

                String picName = DigestUtils.sha1Hex("picture_" + picId + "_!");

                byte[] bytes = item_picture.getBytes();
                Path path = Paths.get(uploadPath + picName + ".jpg");

                Files.write(path, bytes);

                ShopItem shopItem = shopItemService.getItem(item_id);

                Picture picture = new Picture(null, picName, new Date(), shopItem);

                shopItemService.savePicture(picture);

                return "redirect:/editItem?id=" + item_id + "#pictures";

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "redirect:/editItem?id=" + item_id;
    }


    @PostMapping(value = "/removePhotoFromItem")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String removePhotoFromItem(@RequestParam(name = "item_id") Long item_id,
                                      @RequestParam(name = "picture_id") Long picture_id
    ) {

        ShopItem shopItem = shopItemService.getItem(item_id);
        Picture picture = shopItemService.getPicture(picture_id);

        if (picture != null && shopItem != null) {
            shopItemService.deletePicture(picture_id);
        }

        return "redirect:/editItem?id=" + item_id + "#pictures";
    }

    @GetMapping(value = "/viewPhotoOfItem/{url}", produces = {MediaType.IMAGE_JPEG_VALUE})
    public @ResponseBody
    byte[] viewPhotoOfItem(@PathVariable(name = "url") String photo_url) throws IOException {

        String pictureUrl = viewPath + "noImage";

        if (photo_url != null) {
            pictureUrl = viewPath + photo_url + ".jpg";
        }

        InputStream in;

        try {
            ClassPathResource resource = new ClassPathResource(pictureUrl);
            in = resource.getInputStream();
        } catch (Exception e) {

            ClassPathResource resource = new ClassPathResource(viewPath + defaultPicture);
            in = resource.getInputStream();
            e.printStackTrace();
        }

        return IOUtils.toByteArray(in);

    }

    // add item to basket
    @GetMapping(value = "/addToBasket/{item_id}")
    public String addToBasket(@CookieValue(name = "basket_items_ids", defaultValue = "") String value,
                              HttpServletResponse response,
                              @PathVariable(name = "item_id") Long item_id
    ) {

        ShopItem shopItem = shopItemService.getItem(item_id);
        if (shopItem != null) {
            value += ("_" + item_id);
            Cookie cookie = new Cookie("basket_items_ids", value);
            cookie.setPath("/");
            cookie.setMaxAge(365 * 24 * 60 * 60);
            response.addCookie(cookie);
        }

        return "redirect:/basket";
    }

    @GetMapping(value = "/deleteFromBasket/{item_id}")
    public String deleteFromBasket(
                              @CookieValue(name = "basket_items_ids", defaultValue = "") String value,
                              HttpServletResponse response,
                              @PathVariable(name = "item_id") Long item_id
    ) {

        ShopItem shopItem = shopItemService.getItem(item_id);

        if (shopItem != null) {

            int idx = value.indexOf(String.valueOf(item_id));
            int idx3 = value.indexOf('_' ,idx);
            if (idx3 < 0){
                idx3 = value.length();
            }

            String q = value.substring(0, idx);
            String w = value.substring(idx3);

            Cookie cookie = new Cookie("basket_items_ids", q+w);
            cookie.setPath("/");
            cookie.setMaxAge(365 * 24 * 60 * 60);
            response.addCookie(cookie);
        }

        return "redirect:/basket";
    }

    //basket page
    @GetMapping(value = "/basket")
    public String basket(Model model,
                         @CookieValue(name = "basket_items_ids", defaultValue = "") String value
    ) {
        ArrayList<ShopItem> items = new ArrayList<>();

        if (value.contains("_")) {
            String[] num = value.split("_");

            for (int i = 0; i < num.length; i++) {
                if (!num[i].equals("")) {
                    if (shopItemService.getItem(Long.parseLong(num[i])) != null) {
                        items.add(shopItemService.getItem(Long.parseLong(num[i])));
                    }
                }
            }
        }

        int total = 0;
        Map<ShopItem, Integer> counter = new LinkedHashMap<>();
        for (ShopItem x : items) {
            total+= x.getPrice();
            int newValue = counter.getOrDefault(x, 0) + 1;
            counter.put(x, newValue);
        }


        model.addAttribute("items", counter);
        model.addAttribute("currentUser", getUserData());
        model.addAttribute("total", total);

        List<Brand> brands = shopItemService.allBrands();

        model.addAttribute("brands", brands);

        return "basket";
    }


//    @PostMapping(value = "/purchaseItem")
    @GetMapping(value = "/purchaseItem")
    public String purchaseItem(@RequestParam(name = "full_name") String full_name,
                               @RequestParam(name = "card_number") String card_number,
                               @RequestParam(name = "expiration") String expiration,
                               @RequestParam(name = "cvv") int cvv,
                               @CookieValue(name = "basket_items_ids", defaultValue = "") String value,
                               HttpServletResponse response
    ) {

        ArrayList<ShopItem> items = new ArrayList<>();

        if (value.contains("_")) {
            String[] num = value.split("_");

            for (int i = 0; i < num.length; i++) {
                if (!num[i].equals("")) {
                    if (shopItemService.getItem(Long.parseLong(num[i])) != null) {
                        items.add(shopItemService.getItem(Long.parseLong(num[i])));
                    }
                }
            }
        }


        for (ShopItem shopItem: items) {
            Purchase purchase = new Purchase(null, full_name, card_number, expiration, cvv, new Date(),shopItem);
            shopItemService.savePurchase(purchase);
        }

        Cookie cookie = new Cookie("basket_items_ids", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);


        return "redirect:/";
    }


    @GetMapping(value = "/clearBasket")
    public String clearBasket(
            HttpServletResponse response
    ) {

        Cookie cookie = new Cookie("basket_items_ids", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/basket";
    }

    @PostMapping(value = "/addComment")
    @PreAuthorize("isAuthenticated()")
    public String addComment(@RequestParam(name = "item_id") Long item_id,
                             @RequestParam(name = "comment") String comment
    ) {


        Comment comment123 = new Comment(null, comment, new Date(), shopItemService.getItem(item_id), shopUserService.getShopUserById(getUserData().getId()));

        shopItemService.saveComment(comment123);

        return "redirect:/itemDetails/" + item_id + "#comments";
    }

    @PostMapping(value = "/deleteComment")
    @PreAuthorize("isAuthenticated()")
    public String deleteComment(@RequestParam(name = "item_id") Long item_id,
                             @RequestParam(name = "comment_id") Long comment_id
    ) {

        Comment comment = shopItemService.getComment(comment_id);

        if (comment.getAuthor().getId().equals(Objects.requireNonNull(getUserData()).getId()) || getUserData().getRoles().size() > 1){
            shopItemService.deleteComment(comment_id);
        }

        return "redirect:/itemDetails/" + item_id + "#comments";

    }

    private static Long editing_comment_id = 0L;

    @PostMapping(value = "/toEditComment")
    @PreAuthorize("isAuthenticated()")
    public String editComment(
                                 @RequestParam(name = "comment_id") Long comment_id,
                                 @RequestParam(name = "item_id") Long item_id
    ) {
        Comment comment = shopItemService.getComment(comment_id);
        if (comment.getAuthor().getId().equals(Objects.requireNonNull(getUserData()).getId())){
            editing_comment_id = comment_id;
        }

        return "redirect:/itemDetails/" + item_id + "#comments";
    }



    @PostMapping(value = "/updateComment")
    @PreAuthorize("isAuthenticated()")
    public String updateComment(
            @RequestParam(name = "comment") String comment_text,
            @RequestParam(name = "comment_id") Long comment_id,
            @RequestParam(name = "item_id") Long item_id
    ) {

        Comment comment = shopItemService.getComment(comment_id);

        if (comment.getAuthor().getId().equals(getUserData().getId())){
            comment.setComment(comment_text);
            shopItemService.saveComment(comment);
        }
        editing_comment_id = 0L;
        return "redirect:/itemDetails/" + item_id + "#comments";

    }

    @GetMapping(value = "/categories/{cat}")
    public String categories(Model model,
            @PathVariable(name = "cat") String cat
    ) {

//        List<ShopItem> items = shopItemService.

       return "categories";
    }

}
