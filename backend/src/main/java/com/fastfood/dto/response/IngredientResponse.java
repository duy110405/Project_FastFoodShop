package com.fastfood.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IngredientResponse {
    String idIngredient;
    String imageUrlIngredient;
    String ingredientName;
    String unit;
    int quantityStock;
}
