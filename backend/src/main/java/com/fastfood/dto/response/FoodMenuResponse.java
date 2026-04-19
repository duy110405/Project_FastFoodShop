package com.fastfood.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodMenuResponse {
    private String idFood;
    private String imageUrlFood;
    private String foodName;
    private BigDecimal unitPrice;
    private String description;
    private boolean isAvailable; // Cờ báo hiệu món này còn bán được không
}