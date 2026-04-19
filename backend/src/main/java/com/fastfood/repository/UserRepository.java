package com.fastfood.repository;

import com.fastfood.entity.system.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role")
    List<User> findAllWithRole();
}