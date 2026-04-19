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
public class CashierOrderItemResponse {
    private Long orderDetailId;
    private String foodId;
    private String foodName;
    private String imageUrlFood;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}

