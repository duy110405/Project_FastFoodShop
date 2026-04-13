package com.fastfood.dto.request;

import lombok.Data;

@Data
public class FoodIngredientRequest {
    private String idIngredient;
    private Double quantityUsed;
}