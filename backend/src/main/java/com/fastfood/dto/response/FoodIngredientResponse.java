package com.fastfood.dto.response;

import lombok.Data;

@Data
public class FoodIngredientResponse {
    private String idIngredient;
    private String ingredientName; // Nhờ MapStruct móc sang lấy tên nguyên liệu
    private String unit;
    private Double quantityUsed;
}
