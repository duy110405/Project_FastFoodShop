package com.fastfood.service;

import java.util.List;

import com.fastfood.dto.request.FoodCategoryRequest;
import com.fastfood.dto.response.FoodCategoryResponse;

public interface IFoodCategoryService {
    List<FoodCategoryResponse> getAllCategory();
    FoodCategoryResponse getCategoryById(String idCategory);
    FoodCategoryResponse saveCategory(FoodCategoryRequest request);
    FoodCategoryResponse updateCategory(String idCategory, FoodCategoryRequest request);
    void deleteCategory(String idCategory);
    String generateNextIdCategory();
}