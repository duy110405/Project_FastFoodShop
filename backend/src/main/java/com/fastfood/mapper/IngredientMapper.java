package com.fastfood.mapper;

import com.fastfood.dto.request.IngredientRequest;
import com.fastfood.dto.response.IngredientResponse;
import com.fastfood.entity.catalog.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    // chuyển từ  entity sang response
    IngredientResponse toIngredientResponse (Ingredient ingredient);

    // chuyển từ request sang entity
    Ingredient toIngredientEntity (IngredientRequest ingredientRequest);

    // Hàm update
    void updateIngredienFromRequest (IngredientRequest ingredientRequest , @MappingTarget Ingredient ingredient);
}
