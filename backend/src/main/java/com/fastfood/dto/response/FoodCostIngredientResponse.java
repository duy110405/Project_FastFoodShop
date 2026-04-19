package com.fastfood.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodCostIngredientResponse {
    private String ingredientId;
    private String ingredientName;
    private String unit;
    private BigDecimal quantityUsed;
    private BigDecimal importPrice;
    private BigDecimal costAmount;
}

