package com.fastfood.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fastfood.entity.transaction.OrderDetail;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("SELECT od.food.idFood, od.food.foodName, od.food.imageUrlFood, SUM(od.quantity) " +
            "FROM OrderDetail od " +
            "WHERE od.order.status = 'PENDING' AND od.status = 'PENDING' " +
            "GROUP BY od.food.idFood, od.food.foodName, od.food.imageUrlFood " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> findPendingFoodQuantities();

    @Query("""
        select
            function('date', od.order.orderTime),
            i.idIngredient,
            i.imageUrlIngredient,
            i.ingredientName,
            i.unit,
            i.quantityStock,
            sum(fi.quantityUsed * od.quantity)
        from OrderDetail od
        join FoodIngredient fi on fi.food = od.food
        join fi.ingredient i
        where od.order.status = 'PENDING'
          and (:fromDate is null or function('date', od.order.orderTime) >= :fromDate)
          and (:toDate is null or function('date', od.order.orderTime) <= :toDate)
        group by
            function('date', od.order.orderTime),
            i.idIngredient,
            i.imageUrlIngredient,
            i.ingredientName,
            i.unit,
            i.quantityStock
        order by function('date', od.order.orderTime) desc, i.idIngredient asc
    """)
    List<Object[]> getIngredientConsumptionHistory(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}