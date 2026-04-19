package com.fastfood.repository;

import com.fastfood.entity.transaction.OrderDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("SELECT od.food.idFood, od.food.foodName, od.food.imageUrlFood, SUM(od.quantity) " +
            "FROM OrderDetail od " +
            "WHERE od.order.status = 'PENDING' AND od.status = 'PENDING' " +
            "GROUP BY od.food.idFood, od.food.foodName, od.food.imageUrlFood " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> findPendingFoodQuantities();

    @Query("""
            SELECT od.food.idFood,
                   od.food.foodName,
                   od.food.imageUrlFood,
                   SUM(od.quantity),
                   SUM(od.unitPrice * od.quantity)
            FROM OrderDetail od
            WHERE od.order.orderTime >= :fromDateTime
              AND od.order.orderTime < :toDateTime
            GROUP BY od.food.idFood, od.food.foodName, od.food.imageUrlFood
            ORDER BY SUM(od.quantity) DESC
            """)
    List<Object[]> findTopOrderedFoodsInRange(@Param("fromDateTime") LocalDateTime fromDateTime,
                                              @Param("toDateTime") LocalDateTime toDateTime,
                                              Pageable pageable);

    @Query("""
            SELECT FUNCTION('DATE', od.order.orderTime),
                   fi.ingredient.idIngredient,
                   fi.ingredient.imageUrlIngredient,
                   fi.ingredient.ingredientName,
                   fi.ingredient.unit,
                   fi.ingredient.quantityStock,
                   SUM(fi.quantityUsed * od.quantity)
            FROM OrderDetail od
            JOIN FoodIngredient fi ON fi.food = od.food
            WHERE od.order.orderTime >= :fromDateStart
              AND od.order.orderTime < :toDateExclusive
            GROUP BY FUNCTION('DATE', od.order.orderTime),
                     fi.ingredient.idIngredient,
                     fi.ingredient.imageUrlIngredient,
                     fi.ingredient.ingredientName,
                     fi.ingredient.unit,
                     fi.ingredient.quantityStock
            ORDER BY FUNCTION('DATE', od.order.orderTime) DESC,
                     SUM(fi.quantityUsed * od.quantity) DESC
            """)
    List<Object[]> getIngredientConsumptionHistory(@Param("fromDateStart") LocalDateTime fromDateStart,
                                                   @Param("toDateExclusive") LocalDateTime toDateExclusive);

    default List<Object[]> getIngredientConsumptionHistory(LocalDate fromDate, LocalDate toDate) {
        LocalDate resolvedFrom = fromDate != null ? fromDate : LocalDate.now();
        LocalDate resolvedTo = toDate != null ? toDate : resolvedFrom;
        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new IllegalArgumentException("fromDate không được lớn hơn toDate");
        }
        return getIngredientConsumptionHistory(
                resolvedFrom.atStartOfDay(),
                resolvedTo.plusDays(1).atStartOfDay()
        );
    }
}

