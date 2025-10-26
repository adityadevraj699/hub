package com.fashion.hub.Repository;

import java.util.List;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Producer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fashion.hub.Model.CartItem;
import com.fashion.hub.Model.Product;
import com.fashion.hub.Model.User;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    CartItem findByUserAndProduct(User user, Producer product);
	CartItem findByUserAndProduct(User currentUser, Product product);
	void deleteByUser(User user);
}