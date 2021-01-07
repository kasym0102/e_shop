package com.webApp.e_shop.services;

import com.webApp.e_shop.entities.Role;
import com.webApp.e_shop.entities.ShopUser;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface ShopUserService extends UserDetailsService {
    ShopUser getShopUserByEmail(String email);
    ShopUser getShopUserById(Long Id);

    ShopUser createUser(ShopUser shopUser);
    ShopUser createModerator(ShopUser shopUser);

    List<ShopUser> allUsers();

    boolean saveUser(ShopUser shopUser);

    boolean updateProfile(ShopUser user);

    boolean updatePassword(ShopUser shopUser);

    Role getRoleByName(String name);
    boolean saveRole(Role role);

    List<Role> allRoles();

}
