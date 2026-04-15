package com.fastfood.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String tableNumber;
    private String customerName;
    private String createdBy;
    private List<OrderItemDto> items;

    @Data
    public static class OrderItemDto {
        private String foodId;
        private Integer quantity;
    }
}
