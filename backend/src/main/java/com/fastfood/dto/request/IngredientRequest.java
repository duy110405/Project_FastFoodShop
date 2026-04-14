package com.fastfood.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IngredientRequest {
    String imageUrlIngredient;
    String ingredientName;
    String unit;
    int quantityStock;
}
