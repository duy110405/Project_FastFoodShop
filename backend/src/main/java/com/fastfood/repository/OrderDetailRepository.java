package com.fastfood.repository;

import com.fastfood.entity.transaction.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("SELECT od.food.idFood, od.food.foodName, od.food.imageUrlFood, SUM(od.quantity) " +
            "FROM OrderDetail od " +
            "WHERE od.order.status = 'PENDING' AND od.status = 'PENDING' " +
            "GROUP BY od.food.idFood, od.food.foodName, od.food.imageUrlFood " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> findPendingFoodQuantities();
}

