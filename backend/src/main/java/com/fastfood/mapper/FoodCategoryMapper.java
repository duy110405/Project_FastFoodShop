package com.fastfood.mapper;

import com.fastfood.dto.request.FoodCategoryRequest;
import com.fastfood.dto.response.FoodCategoryResponse;
import com.fastfood.entity.catalog.FoodCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FoodCategoryMapper {

    FoodCategoryResponse toFoodCategoryResponse(FoodCategory foodCategory);

    FoodCategory toFoodCategory (FoodCategoryRequest foodCategoryRequest);
    // Hàm update
    void updateFoodCategoryFromRequest (FoodCategoryRequest foodIngredientRequest, @MappingTarget FoodCategory foodCategory);
}

