package com.fastfood.service;

import com.fastfood.dto.request.FoodCategoryRequest;
import com.fastfood.dto.response.FoodCategoryResponse;

import java.util.List;

public interface IFoodCategoryService {
    List<FoodCategoryResponse> getAllCategory();
    FoodCategoryResponse getCategoryById(String idCategory);
    // hàm tự sinh mã
    String generateNextIdCategory();
    FoodCategoryResponse saveCategory(FoodCategoryRequest foodCategoryRequest);
    FoodCategoryResponse updateCategory(String idCategory , FoodCategoryRequest foodCategoryRequest);
    void deleteCategory(String idCategory);
}
