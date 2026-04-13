package com.fastfood.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FoodKitchenResponse {
    private String idFood;
    private String foodName;
    private BigDecimal unitPrice;
    // danh sách nguyên liệu
    private List<FoodIngredientResponse> ingredients;
}
