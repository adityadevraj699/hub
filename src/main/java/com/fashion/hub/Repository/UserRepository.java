package com.fashion.hub.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fashion.hub.Model.Role;
import com.fashion.hub.Model.User;

public interface UserRepository extends JpaRepository<User,Long> {

	List<User> findByRole(Role customer);

}
