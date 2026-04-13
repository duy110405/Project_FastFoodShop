package com.fastfood.repository;

import com.fastfood.entity.catalog.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodCategoryRepository extends JpaRepository<FoodCategory,String > {
    // Hàm này để hỗ trợ việc tự động sinh mã
    @Query("SELECT MAX(fc.idCategory) FROM FoodCategory fc")
    String findMaxidCategory();
}
