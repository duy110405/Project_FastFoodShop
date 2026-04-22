package com.fastfood.dto.response;

import lombok.Data;

@Data
public class KitchenOrderItemResponse {
    private Long orderDetailId;
    private String orderId;
    private String foodId;
    private String foodName;
    private String imageUrlFood;
    private Integer quantity;
}

