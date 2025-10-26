package com.fashion.hub.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fashion.hub.Model.Order;
import com.fashion.hub.Model.Order.Status;
import com.fashion.hub.Model.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

	List<Order> findByStatus(Status status);
}

