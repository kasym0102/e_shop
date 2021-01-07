package com.webApp.e_shop.repositories;

import com.webApp.e_shop.entities.Comment;
import com.webApp.e_shop.entities.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByShopItemId(Long id);

    Comment findAllById(Long id);

}
