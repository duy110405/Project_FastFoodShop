package com.fastfood.repository;

import com.fastfood.entity.catalog.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient , String> {
    // Hàm này để hỗ trợ việc tự động sinh mã
    @Query("SELECT MAX(igd.idIngredient) FROM Ingredient igd")
    String findMaxidIngredient();
}
