package com.fastfood.dto.response;

import lombok.Data;

@Data
public class KitchenFoodPendingResponse {
    private String foodId;
    private String foodName;
    private String imageUrlFood;
    private Integer remainingQuantity;
}


