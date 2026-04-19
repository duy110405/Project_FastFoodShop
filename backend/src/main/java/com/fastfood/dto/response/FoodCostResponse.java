package com.fastfood.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodCostResponse {
    private String idFood;
    private String foodName;
    private BigDecimal salePrice;
    private BigDecimal productionCost;
    private BigDecimal grossProfit;
    private BigDecimal marginPercent;
    private List<FoodCostIngredientResponse> ingredients;
}

