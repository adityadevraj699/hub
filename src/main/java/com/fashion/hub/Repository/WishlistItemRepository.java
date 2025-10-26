package com.fashion.hub.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fashion.hub.Model.Product;
import com.fashion.hub.Model.User;
import com.fashion.hub.Model.WishlistItem;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUser(User user);
    WishlistItem findByUserAndProduct(User user, Product product);
}