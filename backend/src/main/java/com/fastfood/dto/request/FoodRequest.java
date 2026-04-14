package com.fastfood.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FoodRequest {
    private String imageUrlFood;
    private String foodName;
    private String idCategory;
    private BigDecimal unitPrice;
    // Danh sách nguyên liệu mà FE gửi xuống cùng lúc tạo món
    private List<FoodIngredientRequest> ingredients;
}
