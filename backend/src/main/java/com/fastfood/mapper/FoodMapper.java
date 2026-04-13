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

    @Mapping(target = "idFood", ignore = true)
    @Mapping(target = "foodCategory.idCategory", source = "idCategory")
    @Mapping(target = "foodIngredients", source = "ingredients")
    Food toFoodEntity(FoodRequest foodRequest);

    // FIX 2: Dạy MapStruct cách nhét chuỗi "NL001" vào Object Ingredient
    @Mapping(target = "ingredient.idIngredient", source = "idIngredient")
    @Mapping(target = "food", ignore = true) // Món ăn sẽ được gán ở Service (ép con nhận cha)
    FoodIngredient toFoodIngredientEntity(FoodIngredientRequest request);



    // Trả cho Khách xem Menu (Không có list nguyên liệu)
    FoodMenuResponse toFoodMenuResponse(Food food);

    // Trả cho Nhà bếp (Có kèm list nguyên liệu)
    @Mapping(source = "foodIngredients", target = "ingredients") // Nối mảng đầu ra
    FoodKitchenResponse toFoodKitchenResponse(Food food);

    // Dạy MapStruct cách móc dữ liệu từ bảng Ingredient để trả ra chi tiết (Tên, đơn vị)
    @Mapping(source = "ingredient.idIngredient", target = "idIngredient")
    @Mapping(source = "ingredient.ingredientName", target = "ingredientName")
    @Mapping(source = "ingredient.unit", target = "unit")
    FoodIngredientResponse toFoodIngredientResponse(FoodIngredient fi);


    @Mapping(target = "idFood", ignore = true)
    @Mapping(target = "foodCategory.idCategory", source = "idCategory")
    @Mapping(target = "foodIngredients", source = "ingredients")
    void updateFoodFromRequest(FoodRequest foodRequest, @MappingTarget Food food);
}