package com.fastfood.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fastfood.entity.catalog.FoodCategory;

public interface FoodCategoryRepository extends JpaRepository<FoodCategory, String> {

    @Query("select max(f.idCategory) from FoodCategory f")
    String findMaxIdCategory();
}