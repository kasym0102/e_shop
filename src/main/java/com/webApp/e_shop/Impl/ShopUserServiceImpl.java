package com.webApp.e_shop.Impl;

import com.webApp.e_shop.entities.Role;
import com.webApp.e_shop.entities.ShopUser;
import com.webApp.e_shop.repositories.RoleRepository;
import com.webApp.e_shop.repositories.ShopUserRepository;
import com.webApp.e_shop.services.ShopUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShopUserServiceImpl implements ShopUserService {

    @Autowired
    private ShopUserRepository shopUserRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public ShopUser getShopUserByEmail(String email) {
        return shopUserRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        ShopUser shopUser = shopUserRepository.findByEmail(s);
        if (shopUser != null){
            User user = new User(shopUser.getEmail(), shopUser.getPassword(), shopUser.getRoles() );
            return user;
        }
        throw  new UsernameNotFoundException("Username NOT FOUND!");
    }

    @Override
    public ShopUser createUser(ShopUser shopUser) {
        if (shopUserRepository.findByEmail(shopUser.getEmail()) == null) {

            Role role = roleRepository.findByName("ROLE_USER");

            if (role != null){
                ArrayList<Role> roles = new ArrayList<>();
                roles.add(role);
                shopUser.setRoles(roles);
                shopUser.setPassword(passwordEncoder.encode(shopUser.getPassword()));


                return shopUserRepository.save(shopUser);
            }
        }
        return null;
    }

    @Override
    public ShopUser createModerator(ShopUser shopUser) {
        if (shopUserRepository.findByEmail(shopUser.getEmail()) == null) {

            Role role = roleRepository.findByName("ROLE_USER");
            Role role2 = roleRepository.findByName("ROLE_MODERATOR");

            if (role != null){
                ArrayList<Role> roles = new ArrayList<>();
                roles.add(role);
                roles.add(role2);
                shopUser.setRoles(roles);
                shopUser.setPassword(passwordEncoder.encode(shopUser.getPassword()));


                return shopUserRepository.save(shopUser);
            }
        }
        return null;
    }

    @Override
    public List<ShopUser> allUsers() {
        return shopUserRepository.findAll();
    }

    @Override
    public ShopUser getShopUserById(Long Id) {
        return shopUserRepository.getOne(Id);
    }

    @Override
    public boolean saveUser(ShopUser shopUser) {

            shopUserRepository.save(shopUser);
            return true;

    }

    @Override
    public boolean updateProfile(ShopUser user) {
        ShopUser shopUser1 = shopUserRepository.getOne(user.getId());
        if (!shopUser1.getEmail().equals(user.getEmail())){
            ShopUser shopUser = shopUserRepository.findByEmail(user.getEmail());
            if (shopUser != null){
                return  false;
            }
        }
        shopUserRepository.save(user);
        return true ;
    }

    @Override
    public boolean updatePassword(ShopUser shopUser) {

        shopUserRepository.save(shopUser);
        return true ;
    }

    @Override
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public boolean saveRole(Role role) {
        roleRepository.save(role);
        return true;
    }

    @Override
    public List<Role> allRoles() {
        return roleRepository.findAll();
    }
}
