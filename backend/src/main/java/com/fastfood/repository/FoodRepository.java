package com.fastfood.repository;

import com.fastfood.entity.catalog.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food , String> {
    // Hàm này để hỗ trợ việc tự động sinh mã
    @Query("SELECT MAX(f.idFood) FROM Food f")
    String findMaxidFood();

    @Query("SELECT DISTINCT f FROM Food f " +
            "LEFT JOIN FETCH f.foodIngredients fi " +
            "LEFT JOIN FETCH fi.ingredient")
    List<Food> findAllWithIngredients();
}
