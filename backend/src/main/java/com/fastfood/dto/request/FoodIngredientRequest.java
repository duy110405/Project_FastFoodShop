package com.fastfood.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodIngredientRequest {
    private String idIngredient;
    private BigDecimal quantityUsed;
}