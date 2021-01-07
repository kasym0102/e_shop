package com.webApp.e_shop.repositories;

import com.webApp.e_shop.entities.Picture;
import com.webApp.e_shop.entities.ShopUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ShopUserRepository extends JpaRepository<ShopUser, Long> {

    ShopUser findByEmail(String email);


}
