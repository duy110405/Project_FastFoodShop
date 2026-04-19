package com.fastfood.mapper;

import com.fastfood.dto.request.FoodIngredientRequest;
import com.fastfood.dto.request.FoodRequest;
import com.fastfood.dto.response.FoodIngredientResponse;
import com.fastfood.dto.response.FoodKitchenResponse;
import com.fastfood.dto.response.FoodMenuResponse;
import com.fastfood.entity.catalog.Food;
import com.fastfood.entity.catalog.FoodIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    // 1. TẠO MỚI (Tạo mảng List vô tư vì là đồ mới)
    @Mapping(target = "idFood", ignore = true)
    @Mapping(target = "foodCategory.idCategory", source = "idCategory")
    @Mapping(target = "foodIngredients", source = "ingredients")
    Food toFoodEntity(FoodRequest foodRequest);

    @Mapping(target = "ingredient.idIngredient", source = "idIngredient")
    @Mapping(target = "food", ignore = true)
    FoodIngredient toFoodIngredientEntity(FoodIngredientRequest request);


    // 2. LẤY DỮ LIỆU (GET)
    @Mapping(target = "idCategory", source = "foodCategory.idCategory") // Thêm dòng này để fix lỗi Tab Menu hôm nọ nè!
    FoodMenuResponse toFoodMenuResponse(Food food);

    @Mapping(source = "foodIngredients", target = "ingredients")
    FoodKitchenResponse toFoodKitchenResponse(Food food);

    @Mapping(source = "ingredient.idIngredient", target = "idIngredient")
    @Mapping(source = "ingredient.ingredientName", target = "ingredientName")
    @Mapping(source = "ingredient.unit", target = "unit")
    FoodIngredientResponse toFoodIngredientResponse(FoodIngredient fi);

    // 3. CẬP NHẬT (UPDATE) - QUAN TRỌNG NHẤT LÀ ĐOẠN NÀY
    @Mapping(target = "idFood", ignore = true)
    @Mapping(target = "foodCategory.idCategory", source = "idCategory")
    @Mapping(target = "foodIngredients", ignore = true) // CẤM MAPSTRUCT CHẠM VÀO LIST NGUYÊN LIỆU KHI UPDATE!
    void updateFoodFromRequest(FoodRequest foodRequest, @MappingTarget Food food);
}